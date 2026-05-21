package dev.chililisoup.modularsynths.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.AbstractSynthBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.reg.ModRenderTypes;
import dev.chililisoup.modularsynths.reg.ModItems;
import dev.chililisoup.modularsynths.util.ModUtil;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static dev.chililisoup.modularsynths.block.AbstractSynthBlock.PORT_RADIUS;
import static dev.chililisoup.modularsynths.util.ModUtil.HALF_ROOT_TWO;

public final class CablePreviewRenderer {
    public static void submit(LevelRenderContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !player.mayBuild()) return;

        Level level = minecraft.level;
        if (level == null) return;

        ModularSynths.CableDraw cableDraw = player.getAttached(ModularSynths.CABLE_DRAW);
        CameraRenderState camera = context.levelState().cameraRenderState;
        float lineWidth = Math.max(minecraft.getWindow().getHeight() / 300F, 1F);

        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z);

        if (minecraft.hitResult instanceof BlockHitResult hitResult) {
            PortOverlay.extract(level, cableDraw, player, hitResult).ifPresent(portOverlay -> {
                poseStack.pushPose();
                BlockPos pos = hitResult.getBlockPos();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                transformPoseStackToFront(poseStack, portOverlay.orientation);

                portOverlay.render(
                        poseStack.last(),
                        context.bufferSource().getBuffer(RenderTypes.LINES),
                        lineWidth
                );

                poseStack.popPose();
            });
        }

        if (cableDraw != null) {
            BlockHitResult hitResult = minecraft.options.getCameraType().isMirrored() ?
                    ModUtil.getHitResult(player.level(), player, camera.pos, player.getXRot(), player.getYRot()) :
                    ModUtil.getHitResult(player.level(), player, camera.pos, camera.xRot, camera.yRot);

            CableLine cableLine = CableLine.extract(level, cableDraw, hitResult);

            poseStack.pushPose();
            BlockPos pos = cableDraw.pos();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            transformPoseStackToFront(poseStack, cableLine.portOverlay.orientation);

            cableLine.portOverlay.render(
                    poseStack.last(),
                    context.bufferSource().getBuffer(RenderTypes.LINES),
                    lineWidth
            );

            poseStack.popPose();

            drawLine(
                    context.poseStack(),
                    context.bufferSource(),
                    cableDraw.portPos(),
                    cableLine.pos,
                    cableDraw.isInput() ? SynthRenderer.INPUT_COLOR : SynthRenderer.OUTPUT_COLOR,
                    cableLine.color,
                    lineWidth
            );
        }

        poseStack.popPose();
    }

    private static void transformPoseStackToFront(PoseStack poseStack, FrontAndTop orientation) {
        Vec3i norm = orientation.front().getUnitVec3i();
        poseStack.translate(
                (norm.getX() / 2.0) + 0.5,
                (norm.getY() / 2.0) + 0.5,
                (norm.getZ() / 2.0) + 0.5
        );
        poseStack.mulPose(orientation.front().getRotation());
        poseStack.mulPose(Direction.NORTH.getRotation());

        if (orientation.front().getAxis() == Direction.Axis.Y) {
            int dir = orientation.front().getAxisDirection().getStep();
            int rot = (dir + 1) * 90;
            poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(
                    0, 0, dir, orientation.top().toYRot() + rot
            ));
        }

        poseStack.translate(-0.5, -0.5, 0);
    }

    private static void drawLine(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Vector3f startPos,
            Vector3f endPos,
            int startColor,
            int endColor,
            float lineWidth
    ) {
        VertexConsumer buffer = bufferSource.getBuffer(ModRenderTypes.NO_DEPTH_LINES);
        PoseStack.Pose pose = poseStack.last();
        Vector3f norm = startPos.sub(endPos, new Vector3f()).normalize();

        buffer.addVertex(pose, startPos).setColor(startColor).setNormal(pose, norm).setLineWidth(lineWidth);
        buffer.addVertex(pose, endPos).setColor(endColor).setNormal(pose, norm).setLineWidth(lineWidth);
    }

    private record CableLine(int color, Vector3f pos, PortOverlay portOverlay) {
        private static CableLine extract(Level level, ModularSynths.CableDraw cableDraw, BlockHitResult hitResult) {
            Vec2 portPos;
            BlockState blockState = level.getBlockState(cableDraw.pos());
            if (blockState.getBlock() instanceof AbstractSynthBlock<?> synthBlock) {
                Vec2[] portPositions = cableDraw.isInput() ?
                        synthBlock.inputPositions() :
                        synthBlock.outputPositions();

                portPos = portPositions.length > cableDraw.port() ?
                        portPositions[cableDraw.port()] :
                        new Vec2(0.5F, 0.5F);
            } else portPos = new Vec2(0.5F, 0.5F);

            CableLine fallback = new CableLine(
                    SynthRenderer.BASE_COLOR,
                    hitResult.getLocation().toVector3f(),
                    new PortOverlay(
                            portPos,
                            cableDraw.isInput() ? SynthRenderer.INPUT_COLOR : SynthRenderer.OUTPUT_COLOR,
                            false,
                            blockState.getValueOrElse(AbstractSynthBlock.ORIENTATION, FrontAndTop.NORTH_UP)
                    )
            );

            BlockPos blockPos = hitResult.getBlockPos();
            if (hitResult.getType() == HitResult.Type.MISS
                    || !(level.getBlockEntity(blockPos) instanceof SynthBlockEntity synthBlockEntity)
                    || !(synthBlockEntity.getBlockState().getBlock() instanceof AbstractSynthBlock<?> synthBlock)
            ) return fallback;

            FrontAndTop orientation = synthBlockEntity.getBlockState()
                    .getValueOrElse(AbstractSynthBlock.ORIENTATION, FrontAndTop.NORTH_UP);
            Optional<Vec2> hitPos = AbstractSynthBlock.getHitPos(hitResult, orientation);
            if (hitPos.isEmpty()) return fallback;

            Vec2[] inputPositions = synthBlock.inputPositions();
            int inPort = AbstractSynthBlock.hitPort(hitPos.get(), inputPositions);
            if (inPort >= 0) {
                if (cableDraw.isInput()) return fallback;

                return new CableLine(
                        SynthRenderer.INPUT_COLOR,
                        AbstractSynthBlock.face3DPos(
                                inputPositions[inPort], orientation
                        ).add(new Vec3(blockPos)).toVector3f(),
                        fallback.portOverlay
                );
            }

            Vec2[] outputPositions = synthBlock.outputPositions();
            int outPort = AbstractSynthBlock.hitPort(hitPos.get(), outputPositions);
            return outPort >= 0 && cableDraw.isInput() ?
                    new CableLine(
                            SynthRenderer.OUTPUT_COLOR,
                            AbstractSynthBlock.face3DPos(
                                    outputPositions[outPort], orientation
                            ).add(new Vec3(blockPos)).toVector3f(),
                            fallback.portOverlay
                    ) : fallback;
        }
    }

    private record PortOverlay(Vec2 pos, int color, boolean negative, FrontAndTop orientation) {
        private static Optional<PortOverlay> extract(
                Level level, ModularSynths.@Nullable CableDraw cableDraw, LocalPlayer player, BlockHitResult hitResult
        ) {
            BlockPos blockPos = hitResult.getBlockPos();
            if (!(level.getBlockEntity(blockPos) instanceof SynthBlockEntity synthBlockEntity))
                return Optional.empty();

            BlockState blockState = synthBlockEntity.getBlockState();
            if (!(blockState.getBlock() instanceof AbstractSynthBlock<?> synthBlock))
                return Optional.empty();

            FrontAndTop orientation = blockState
                    .getValueOrElse(AbstractSynthBlock.ORIENTATION, FrontAndTop.NORTH_UP);
            Optional<Vec2> hitPos = AbstractSynthBlock.getHitPos(hitResult, orientation);
            if (hitPos.isEmpty()) return Optional.empty();

            boolean hasCable = player.getMainHandItem().is(ModItems.PATCH_CABLE);
            Vec2[] inputPositions = synthBlock.inputPositions();
            int inPort = AbstractSynthBlock.hitPort(hitPos.get(), inputPositions);
            if (inPort >= 0) {
                if (cableDraw != null && cableDraw.isInput())
                    return cableDraw.port() != inPort || !cableDraw.pos().equals(blockPos) ?
                            Optional.of(new PortOverlay(
                                    inputPositions[inPort],
                                    SynthRenderer.BASE_COLOR,
                                    true,
                                    orientation
                            )) : Optional.empty();

                if (hasCable) return Optional.of(new PortOverlay(
                        inputPositions[inPort],
                        SynthRenderer.INPUT_COLOR,
                        false,
                        orientation
                ));

                boolean empty = synthBlockEntity.synth.inputEmpty(inPort);
                return Optional.of(new PortOverlay(
                        inputPositions[inPort],
                        empty ? SynthRenderer.BASE_COLOR : SynthRenderer.REMOVE_COLOR,
                        empty,
                        orientation
                ));
            }

            Vec2[] outputPositions = synthBlock.outputPositions();
            int outPort = AbstractSynthBlock.hitPort(hitPos.get(), outputPositions);
            if (outPort >= 0) {
                if (cableDraw != null && !cableDraw.isInput())
                    return cableDraw.port() != outPort || !cableDraw.pos().equals(blockPos) ?
                            Optional.of(new PortOverlay(
                                    outputPositions[outPort],
                                    SynthRenderer.BASE_COLOR,
                                    true,
                                    orientation
                            )) : Optional.empty();

                if (hasCable) return Optional.of(new PortOverlay(
                        outputPositions[outPort],
                        SynthRenderer.OUTPUT_COLOR,
                        false,
                        orientation
                ));

                boolean empty = synthBlockEntity.synth.outputEmpty(outPort);
                return Optional.of(new PortOverlay(
                        outputPositions[outPort],
                        empty ? SynthRenderer.BASE_COLOR : SynthRenderer.REMOVE_COLOR,
                        empty,
                        orientation
                ));
            }

            return Optional.empty();
        }

        private void render(PoseStack.Pose pose, VertexConsumer buffer, float lineWidth) {
            Float[][] points = this.negative ? new Float[][]{
                    {-PORT_RADIUS, -PORT_RADIUS, HALF_ROOT_TWO, HALF_ROOT_TWO},
                    {PORT_RADIUS, PORT_RADIUS, HALF_ROOT_TWO, HALF_ROOT_TWO},

                    {-PORT_RADIUS, PORT_RADIUS, HALF_ROOT_TWO, -HALF_ROOT_TWO},
                    {PORT_RADIUS, -PORT_RADIUS, HALF_ROOT_TWO, -HALF_ROOT_TWO},
            } : new Float[][]{
                    {-PORT_RADIUS, -PORT_RADIUS, 1F, 0F},
                    {PORT_RADIUS, -PORT_RADIUS, 1F, 0F},

                    {PORT_RADIUS, -PORT_RADIUS, 0F, 1F},
                    {PORT_RADIUS, PORT_RADIUS, 0F, 1F},

                    {PORT_RADIUS, PORT_RADIUS, 1F, 0F},
                    {-PORT_RADIUS, PORT_RADIUS, 1F, 0F},

                    {-PORT_RADIUS, PORT_RADIUS, 0F, 1F},
                    {-PORT_RADIUS, -PORT_RADIUS, 0F, 1F}
            };

            for (Float[] point : points) buffer
                    .addVertex(pose, this.pos.x - point[0], this.pos.y - point[1], 0)
                    .setColor(this.color)
                    .setNormal(pose, point[2], point[3], 0)
                    .setLineWidth(lineWidth);
        }
    }
}
