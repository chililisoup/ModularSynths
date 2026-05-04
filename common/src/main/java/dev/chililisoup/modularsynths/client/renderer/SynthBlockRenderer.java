package dev.chililisoup.modularsynths.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.item.PatchCableItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.util.Optional;
import java.util.OptionalDouble;

import static dev.chililisoup.modularsynths.block.SynthBlock.PORT_RADIUS;

public class SynthBlockRenderer implements BlockEntityRenderer<SynthBlockEntity> {
    private static final RenderType SELECTED_CABLE_RENDER_TYPE;
    private static final RenderType HIDDEN_CABLE_RENDER_TYPE;
    private static final RenderType NO_DEPTH_LINES;
    public static final Vector4f INPUT_COLOR = new Vector4f(0F, 0.73F, 1F, 1F);
    public static final Vector4f OUTPUT_COLOR = new Vector4f(1F, 0.55F, 0F, 1F);
    public static final Vector4f BASE_COLOR = new Vector4f(0.8F, 0.8F, 0.8F, 1F);
    public static final Vector4f ADD_COLOR = new Vector4f(0.13F, 1F, 0.29F, 1F);
    public static final Vector4f REMOVE_COLOR = new Vector4f(0.97F, 0.18F, 0.25F, 1F);
    private final BlockEntityRendererProvider.Context context;

    public SynthBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    /*
    This thing is a bunch of spaghetti that is performing too much functionality.
    It should be cleaned up, with a lot of the functionality moved to where makes sense
     */

    @Override
    public void render(SynthBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            blockEntity.cableSelectionExists = false;
            blockEntity.inputSelection = -1;
            return;
        }

        Player player = (Player) Minecraft.getInstance().getCameraEntity();
        if (player == null) {
            blockEntity.cableSelectionExists = false;
            blockEntity.inputSelection = -1;
            return;
        }

        BlockHitResult hitResult =
                Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult ?
                        blockHitResult : null;

        boolean previewCableRendered = renderPreviewCable(blockEntity, partialTick, poseStack, buffer, player, hitResult, level);

        BlockState blockState = blockEntity.getBlockState();
        BlockPos blockPos = blockEntity.getBlockPos();
        SynthBlock synthBlock = (SynthBlock) blockState.getBlock();

        Optional<Vec2> hitPos = getHitPos(hitResult, blockPos, blockState.getValue(BlockStateProperties.ORIENTATION));

        int targetPort;
        boolean targetPortIsInput;
        if (hitPos.isEmpty()) {
            targetPort = -1;
            targetPortIsInput = false;
        } else {
            int inputPort = SynthBlock.hitPort(hitPos.get(), synthBlock.getInputPositions());
            int outputPort = SynthBlock.hitPort(hitPos.get(), synthBlock.getOutputPositions());
            if (inputPort >= 0 || outputPort >= 0) {
                targetPort = inputPort >= 0 ? inputPort : outputPort;
                targetPortIsInput = inputPort >= 0;
            } else {
                targetPort = -1;
                targetPortIsInput = false;
            }
        }

        if (!previewCableRendered && targetPort >= 0 && player.getAbilities().mayBuild)
            renderPort(blockEntity, poseStack, buffer, player, targetPort, targetPortIsInput);
        else blockEntity.inputSelection = -1;

        blockEntity.cableSelectionExists = !previewCableRendered &&
                player.getAbilities().mayBuild &&
                !targetPortIsInput &&
                blockEntity.getOutputs().stream().anyMatch(output -> output.from == targetPort);

        Vec3 blockPosOrigin = Vec3.atLowerCornerOf(blockPos);
        float time = (float) blockEntity.animationTime + partialTick;

        boolean cableSelectionExists = blockEntity.cableSelectionExists;
        int inputConnection = -1;
        BlockPos selectionPos = null;
        if (!previewCableRendered) for (SynthBlockEntity.Connection output : blockEntity.getOutputs()) {
            if (level.getBlockEntity(output.pos) instanceof SynthBlockEntity outputBlockEntity) {
                if (outputBlockEntity.cableSelectionExists) {
                    cableSelectionExists = true;
                    selectionPos = outputBlockEntity.getBlockPos();
                    break;
                } else if (outputBlockEntity.inputSelection >= 0) {
                    inputConnection = outputBlockEntity.inputSelection;
                    selectionPos = outputBlockEntity.getBlockPos();
                    break;
                }
            }
        }

        for (SynthBlockEntity.Output output : blockEntity.getOutputs()) {
            if (!output.ensureBezier(blockEntity)) continue;

            boolean selectionExists = previewCableRendered ||
                    blockEntity.cableSelectionExists ||
                    blockEntity.inputSelection >= 0 ||
                    ((cableSelectionExists || inputConnection >= 0) && output.pos.equals(selectionPos));

            boolean selected = selectionExists && (
                    (cableSelectionExists && output.from == targetPort) ||
                    (inputConnection >= 0 && output.to == inputConnection && output.pos.equals(selectionPos))
            );

            renderCable(poseStack, buffer, level, blockPosOrigin, output, selectionExists, selected, time);
        }

        synthBlock.render(level, blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay, context);
    }

    public static void transformPoseStackToFront(PoseStack poseStack, FrontAndTop fat, double scale) {
        Vec3i norm = fat.front().getNormal();
        poseStack.translate(
                (norm.getX() / scale) + 0.5,
                (norm.getY() / scale) + 0.5,
                (norm.getZ() / scale) + 0.5
        );
        poseStack.mulPose(fat.front().getRotation());
        poseStack.mulPose(Direction.NORTH.getRotation());

        if (fat.front().getAxis() == Direction.Axis.Y) {
            int dir = fat.front().getAxisDirection().getStep();
            int rot = (dir + 1) * 90;
            poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0, 0, dir, fat.top().toYRot() + rot));
        }
    }

    public static void transformPoseStackToFront(PoseStack poseStack, FrontAndTop fat) {
        transformPoseStackToFront(poseStack, fat, 2.0);
    }

    private static Optional<Vec2> getHitPos(BlockHitResult hitResult, BlockPos blockPos, FrontAndTop fat) {
        if (hitResult == null) return Optional.empty();
        if (hitResult.getType() != HitResult.Type.BLOCK) return Optional.empty();

        if (!hitResult.getBlockPos().equals(blockPos)) return Optional.empty();
        if (hitResult.getDirection() != fat.front()) return Optional.empty();

        return SynthBlock.getHitPosition(hitResult, fat);
    }

    private static void drawSquare(PoseStack poseStack, MultiBufferSource buffer, Vec2 position, Vector4f color) {
        poseStack.pushPose();
        poseStack.translate(-0.5, -0.5, 0);

        VertexConsumer consumer = buffer.getBuffer(NO_DEPTH_LINES);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        Float[][] points = new Float[][]{
                {-PORT_RADIUS, -PORT_RADIUS, 1F, 0F},
                {PORT_RADIUS, -PORT_RADIUS, 1F, 0F},

                {PORT_RADIUS, -PORT_RADIUS, 0F, 1F},
                {PORT_RADIUS, PORT_RADIUS, 0F, 1F},

                {PORT_RADIUS, PORT_RADIUS, 1F, 0F},
                {-PORT_RADIUS, PORT_RADIUS, 1F, 0F},

                {-PORT_RADIUS, PORT_RADIUS, 0F, 1F},
                {-PORT_RADIUS, -PORT_RADIUS, 0F, 1F}
        };

        for (Float[] coord : points) {
            consumer.vertex(matrix, position.x - coord[0], position.y - coord[1], 0).color(color.x, color.y, color.z, color.w).normal(pose.normal(), coord[2], coord[3], 0).endVertex();
        }

        poseStack.popPose();
    }

    private static void drawLine(PoseStack poseStack, MultiBufferSource buffer, Vec3 startPos, Vec3 endPos, Vector4f startColor, Vector4f endColor) {
        VertexConsumer consumer = buffer.getBuffer(NO_DEPTH_LINES);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        float normX = (float)(startPos.x - endPos.x);
        float normY = (float)(startPos.y - endPos.y);
        float normZ = (float)(startPos.z - endPos.z);
        float normLength = Mth.sqrt(normX * normX + normY * normY + normZ * normZ);
        normX /= normLength;
        normY /= normLength;
        normZ /= normLength;

        consumer.vertex(matrix, (float) startPos.x, (float) startPos.y, (float) startPos.z).color(startColor.x, startColor.y, startColor.z, startColor.w).normal(pose.normal(), normX, normY, normZ).endVertex();
        consumer.vertex(matrix, (float) endPos.x, (float) endPos.y, (float) endPos.z).color(endColor.x, endColor.y, endColor.z, endColor.w).normal(pose.normal(), normX, normY, normZ).endVertex();
    }

    private static boolean renderPreviewCable(
            SynthBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            Player player,
            BlockHitResult hitResult,
            Level level
    ) {
        if (!player.getAbilities().mayBuild) {
            blockEntity.endInsertCable();
            return false;
        }

        if (player.getMainHandItem().getItem() instanceof PatchCableItem cableItem) {
            if (cableItem.insertingBlockEntity != blockEntity) {
                if (blockEntity.insertingCableStart != null)
                    blockEntity.endInsertCable();
                return cableItem.insertingBlockEntity != null;
            }
        } else {
            blockEntity.endInsertCable();
            return false;
        }

        if (blockEntity.insertingCableStart == null) return false;

        BlockPos blockPos = blockEntity.getBlockPos();
        BlockState blockState = blockEntity.getBlockState();
        SynthBlock synthBlock = (SynthBlock) blockState.getBlock();

        poseStack.pushPose();
        transformPoseStackToFront(poseStack, blockState.getValue(BlockStateProperties.ORIENTATION));
        drawSquare(
                poseStack,
                buffer,
                (blockEntity.insertingCableIsInput ? synthBlock.getInputPositions() : synthBlock.getOutputPositions())[blockEntity.insertingCableIndex],
                blockEntity.insertingCableIsInput ? INPUT_COLOR : OUTPUT_COLOR
        );
        poseStack.popPose();

        Vec3 endPos;
        Vector4f endColor = BASE_COLOR;

        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            BlockHitResult invalidHitResult = SynthBlock.getHitResult(level, player, partialTick);
            endPos = invalidHitResult.getLocation();
        } else {
            endPos = hitResult.getLocation();

            BlockPos hitBlockPos = hitResult.getBlockPos();
            BlockState hitState = level.getBlockState(hitBlockPos);
            if (hitState.getBlock() instanceof SynthBlock hitSynthBlock) {
                FrontAndTop hitFat = hitState.getValue(BlockStateProperties.ORIENTATION);
                Optional<Vec2> hitPos = SynthBlock.getHitPosition(hitResult, hitFat);
                if (hitPos.isPresent()) {
                    int hitPort = SynthBlock.hitPort(
                            hitPos.get(),
                            blockEntity.insertingCableIsInput ? hitSynthBlock.getOutputPositions() : hitSynthBlock.getInputPositions()
                    );

                    int invalidHitPort = -1;
                    if (hitPort >= 0) {
                        endColor = ADD_COLOR;
                    } else {
                        invalidHitPort = SynthBlock.hitPort(
                                hitPos.get(),
                                blockEntity.insertingCableIsInput ? hitSynthBlock.getInputPositions() : hitSynthBlock.getOutputPositions()
                        );

                        if (invalidHitPort >= 0) {
                            endColor = REMOVE_COLOR;
                        }
                    }

                    if (hitPort >= 0 || invalidHitPort >= 0) {
                        poseStack.pushPose();
                        BlockPos diff = hitBlockPos.subtract(blockPos);
                        poseStack.translate(diff.getX(), diff.getY(), diff.getZ());
                        transformPoseStackToFront(poseStack, hitState.getValue(BlockStateProperties.ORIENTATION));
                        drawSquare(
                                poseStack,
                                buffer,
                                (blockEntity.insertingCableIsInput ^ (hitPort >= 0) ?
                                        hitSynthBlock.getInputPositions() : hitSynthBlock.getOutputPositions()
                                )[hitPort >= 0 ? hitPort : invalidHitPort],
                                endColor
                        );
                        poseStack.popPose();
                    }
                }
            }
        }

        poseStack.pushPose();
        poseStack.translate(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());

        drawLine(poseStack, buffer, blockEntity.insertingCableStart, endPos, blockEntity.insertingCableIsInput ? INPUT_COLOR : OUTPUT_COLOR, endColor);

        poseStack.popPose();

        return true;
    }

    private static void renderPort(SynthBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer, Player player, int port, boolean isInput) {
        BlockState blockState = blockEntity.getBlockState();

        poseStack.pushPose();
        transformPoseStackToFront(poseStack, blockState.getValue(BlockStateProperties.ORIENTATION));
        SynthBlock synthBlock = (SynthBlock) blockState.getBlock();

        boolean holdingCable = player.getMainHandItem().getItem() instanceof PatchCableItem;
        boolean removing = isInput ?
                blockEntity.getInputs().stream().anyMatch(input -> input.to == port) :
                blockEntity.getOutputs().stream().anyMatch(output -> output.from == port);

        drawSquare(
                poseStack,
                buffer,
                (blockEntity.insertingCableIsInput ^ isInput ?
                        synthBlock.getInputPositions() : synthBlock.getOutputPositions()
                )[port],
                holdingCable ? (isInput ? INPUT_COLOR : OUTPUT_COLOR) : (removing ? REMOVE_COLOR : BASE_COLOR)
        );

        poseStack.popPose();

        blockEntity.inputSelection = (removing && isInput) ? port : -1;
    }

    private static void renderCable(PoseStack poseStack, MultiBufferSource buffer, Level level, Vec3 origin, SynthBlockEntity.Output output, boolean selectionExists, boolean selected, float time) {
        poseStack.pushPose();

        VertexConsumer consumer = buffer.getBuffer(selectionExists ? (selected ? SELECTED_CABLE_RENDER_TYPE : HIDDEN_CABLE_RENDER_TYPE) : RenderType.leash());
        Matrix4f matrix = poseStack.last().pose();

        BlockPos startBlock = BlockPos.containing(output.cableStart.add(origin).add(output.cableStartNorm.scale(0.125)));
        BlockPos endBlock = BlockPos.containing(output.cableEnd.add(origin).add(output.cableEndNorm.scale(0.125)));

        int blockLightStart = level.getBrightness(LightLayer.BLOCK, startBlock);
        int blockLightEnd = level.getBrightness(LightLayer.BLOCK, endBlock);
        int skyLightStart = level.getBrightness(LightLayer.SKY, startBlock);
        int skyLightEnd = level.getBrightness(LightLayer.SKY, endBlock);

        for (int i = 0; i < output.bezierPoints.length; i++) {
            addCableVertexPair(
                    consumer,
                    matrix,
                    output,
                    selectionExists,
                    selected,
                    time,
                    blockLightStart,
                    blockLightEnd,
                    skyLightStart,
                    skyLightEnd,
                    -1,
                    i
            );
        }

        for (int i = output.bezierPoints.length - 1; i >= 0; i--) {
            addCableVertexPair(
                    consumer,
                    matrix,
                    output,
                    selectionExists,
                    selected,
                    time,
                    blockLightStart,
                    blockLightEnd,
                    skyLightStart,
                    skyLightEnd,
                    1,
                    i
            );
        }

        poseStack.popPose();
    }

    private static void addCableVertexPair(
            VertexConsumer consumer,
            Matrix4f matrix,
            SynthBlockEntity.Output output,
            boolean selectionExists,
            boolean selected,
            float time,
            int blockLightStart,
            int blockLightEnd,
            int skyLightStart,
            int skyLightEnd,
            int flip,
            int index
    ) {
        float delta = (float) index / (output.bezierPoints.length - 1);
        float selectSin = selected ? (Mth.sin(delta * output.bezierPoints.length * 0.25F - time * 0.15F) + 1) / 2 : 0;

        Vector3f pos = output.bezierPoints[index];
        Vector3f nor = output.bezierNormals[index];
        Vector3f up = output.bezierUps[index];
        Vector3f off = up.add(nor.mul(flip, new Vector3f()), new Vector3f());

        int checkerMultiplier = index % 2 == (flip == 1 ? 1 : 0) ? 0xb4b4b4 : 0xffffff;
        int alpha = selectionExists ? (selected ? 255 : 63) : 255;
        int finalColor = FastColor.ABGR32.color(
                alpha,
                FastColor.ARGB32.multiply(
                        output.color,
                        FastColor.ARGB32.lerp(
                                selectSin,
                                checkerMultiplier,
                                0xffffff
                        )
                )
        );

        int blockLight = Mth.lerpInt(delta, blockLightStart, blockLightEnd);
        int skyLight = Mth.lerpInt(delta, skyLightStart, skyLightEnd);
        int light = selected ? 16777215 : LightTexture.pack(blockLight, skyLight);

        // debug normals visualizer
//        Vector3f col = nor.normalize(new Vector3f()).add(1, 1, 1).mul(0.5F);

        consumer.vertex(matrix, pos.x - off.x, pos.y - off.y, pos.z - off.z).color(finalColor).uv2(light).endVertex();
        consumer.vertex(matrix, pos.x + off.x, pos.y + off.y, pos.z + off.z).color(finalColor).uv2(light).endVertex();
    }

    static {
        SELECTED_CABLE_RENDER_TYPE = RenderType.create(
                "modular_synths_cable_selected",
                DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
                VertexFormat.Mode.TRIANGLE_STRIP,
                256,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderType.RENDERTYPE_LEASH_SHADER)
                        .setTextureState(RenderType.NO_TEXTURE)
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderType.NO_CULL)
                        .setLightmapState(RenderType.LIGHTMAP)
                        .createCompositeState(false)
        );
        HIDDEN_CABLE_RENDER_TYPE = RenderType.create(
                "modular_synths_cable_hidden",
                DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
                VertexFormat.Mode.TRIANGLE_STRIP,
                256,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderType.RENDERTYPE_LEASH_SHADER)
                        .setTextureState(RenderType.NO_TEXTURE)
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderType.NO_CULL)
                        .setLightmapState(RenderType.LIGHTMAP)
                        .setWriteMaskState(RenderType.COLOR_WRITE)
                        .createCompositeState(false)
        );
        NO_DEPTH_LINES = RenderType.create(
                "modular_synths_no_depth_lines",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES,
                256,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                        .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(RenderType.OUTLINE_TARGET)
                        .setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
                        .setDepthTestState(RenderType.NO_DEPTH_TEST)
                        .setCullState(RenderType.NO_CULL)
                        .createCompositeState(false)
        );
    }
}
