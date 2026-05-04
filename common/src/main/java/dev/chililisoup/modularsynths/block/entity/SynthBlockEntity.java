package dev.chililisoup.modularsynths.block.entity;

import dev.architectury.networking.NetworkManager;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.SpeakerBlock;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.block.SynthMonitor;
import dev.chililisoup.modularsynths.client.network.ServerboundCablePacket;
import dev.chililisoup.modularsynths.client.synthesis.AudioStreamSupplier;
import dev.chililisoup.modularsynths.client.synthesis.BaseSoundInstance;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioPlayer;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import dev.chililisoup.modularsynths.reg.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class SynthBlockEntity extends BlockEntity {
    private final ArrayList<Connection> inputs = new ArrayList<>();
    private final ArrayList<Output> outputs = new ArrayList<>();
    public final int inputPortCount;

    @Environment(EnvType.CLIENT) private BaseSoundInstance[] soundInstances;
    @Environment(EnvType.CLIENT) private AudioStreamSupplier audioStreamSupplier;

    @Environment(EnvType.CLIENT) private double samplePosition = 0.0;
    @Environment(EnvType.CLIENT) private final ArrayList<Integer> customIntData = new ArrayList<>();
    @Environment(EnvType.CLIENT) private final ArrayList<Double> customDoubleData = new ArrayList<>();
    @Environment(EnvType.CLIENT) private final ArrayList<double[]> savedStacks = new ArrayList<>();

    @Environment(EnvType.CLIENT) public boolean insertingCableIsInput;
    @Environment(EnvType.CLIENT) public int insertingCableIndex;
    @Environment(EnvType.CLIENT) public Vec3 insertingCableStart;

    @Environment(EnvType.CLIENT) public int animationTime;
    @Environment(EnvType.CLIENT) public boolean cableSelectionExists;
    @Environment(EnvType.CLIENT) public int inputSelection;

    public SynthBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SYNTH.get(), pos, blockState);
        this.inputPortCount = ((SynthBlock) blockState.getBlock()).getInputPositions().length;
    }

    public ArrayList<Connection> getInputs() {
        return this.inputs;
    }
    public ArrayList<Output> getOutputs() {
        return this.outputs;
    }

    // Optimization idea:
    // Maintain an array(list?) of input block entities
    // that is updated whenever inputs is,
    // and then reference that instead of getting the
    // block entity from the level each time
    //
    // Also there's an infinite recursion bug somewhere here.
    // I've only seen it triggered on cable relays,
    // but it most likely exists everywhere.
    @Environment(EnvType.CLIENT)
    private double[] request(
            int size,
            int outputPort,
            int depth,
            boolean beginning,
            HashMap<Long, double[][]> monitorStack,
            HashMap<Long, ArrayList<Integer>> monitorCheckStack
    ) {
        final double[][][] inputStackSet = {new double[this.inputPortCount][size]};

        boolean checkConnections = this.level != null && depth < ModularSynths.MAX_SYNTH_DEPTH;
        if (checkConnections) {
            this.inputs.forEach(input -> {
                if (!(this.level.getBlockEntity(input.pos) instanceof SynthBlockEntity blockEntity)) return;

                double[] inputStack = blockEntity.request(size, input.from, depth + 1, beginning, monitorStack, monitorCheckStack);

                for (int i = 0; i < size; i++) {
                    inputStackSet[0][input.to][i] += inputStack[i];
                }
            });
        }

        if (beginning) {
            this.samplePosition = 0.0;
            this.customIntData.clear();
            this.customDoubleData.clear();
            this.savedStacks.clear();
        }

        BlockState state = this.getBlockState();
        double[] result = ((SynthBlock) state.getBlock()).requestOutputData(inputStackSet[0], size, outputPort, state, this);

        if (!checkConnections) return result;

        long longPos = this.getBlockPos().asLong();
        if (monitorCheckStack.containsKey(longPos) && monitorCheckStack.get(longPos).contains(outputPort))
            return result;

        this.outputs.forEach(output -> {
            if (output.from != outputPort) return;
            if (!(this.level.getBlockEntity(output.pos) instanceof SynthBlockEntity blockEntity)) return;
            if (!(blockEntity.getBlockState().getBlock() instanceof SynthMonitor)) return;

            long outputPos = blockEntity.getBlockPos().asLong();
            double[][] dataStackSet = monitorStack.getOrDefault(outputPos, new double[blockEntity.inputPortCount][size]);
            for (int i = 0; i < size; i++) dataStackSet[output.to][i] += result[i];
            monitorStack.put(outputPos, dataStackSet);
        });

        if (depth == 0) {
            monitorStack.forEach((monitorPos, monitorInputStackSet) -> {
                if (!(this.level.getBlockEntity(BlockPos.of(monitorPos)) instanceof SynthBlockEntity blockEntity)) return;
                if (!(blockEntity.getBlockState().getBlock() instanceof SynthMonitor monitor)) return;
                monitor.monitorInputData(monitorInputStackSet, size, blockEntity.getBlockState(), blockEntity);
            });
        }

        ArrayList<Integer> monitorCheckSet = monitorCheckStack.getOrDefault(longPos, new ArrayList<>());
        monitorCheckSet.add(outputPort);
        monitorCheckStack.put(longPos, monitorCheckSet);

        return result;
    }

    @Environment(EnvType.CLIENT)
    public double[] request(int size, boolean beginning) {
        return this.request(size, 0, 0, beginning, new HashMap<>(), new HashMap<>());
    }

    @Environment(EnvType.CLIENT)
    public double getSamplePosition() {
        return samplePosition;
    }

    @Environment(EnvType.CLIENT)
    public void setSamplePosition(double samplePosition) {
        this.samplePosition = samplePosition;
    }

    @Environment(EnvType.CLIENT)
    public ArrayList<Integer> getCustomIntData() {
        return customIntData;
    }

    @Environment(EnvType.CLIENT)
    public ArrayList<Double> getCustomDoubleData() {
        return customDoubleData;
    }

    @Environment(EnvType.CLIENT)
    public ArrayList<double[]> getSavedStacks() {
        return savedStacks;
    }

    @Environment(EnvType.CLIENT)
    public void beginAudioStream() {
        Level level = this.getLevel();
        if (level == null) return;

        this.audioStreamSupplier = new AudioStreamSupplier(this);

        this.soundInstances = this.outputs.stream().filter(output ->
                level.getBlockState(output.pos).getBlock() instanceof SpeakerBlock
        ).map(output -> SynthesizedAudioPlayer.playSound(
                output.pos.getX(),
                output.pos.getY(),
                output.pos.getZ(),
                this.audioStreamSupplier
        )).toArray(BaseSoundInstance[]::new);
    }

    @Environment(EnvType.CLIENT)
    public void endAudioStream() {
        if (this.soundInstances == null) return;

        for (BaseSoundInstance soundInstance : this.soundInstances)
            soundInstance.stopStreaming();

        this.soundInstances = null;
    }

    @Environment(EnvType.CLIENT)
    public void beginInsertCable(boolean isInput, int portIndex, Vec3 drawStart) {
        this.insertingCableIsInput = isInput;
        this.insertingCableIndex = portIndex;
        this.insertingCableStart = drawStart;
        this.cableSelectionExists = false;
    }

    @Environment(EnvType.CLIENT)
    public void finishInsertCable(BlockPos target, boolean isInput, int portIndex) {
        if (isInput != this.insertingCableIsInput) {
            if (this.insertingCableIsInput) {
                NetworkManager.sendToServer(ServerboundCablePacket.id(), ServerboundCablePacket.make(
                        target,
                        this.getBlockPos(),
                        portIndex,
                        this.insertingCableIndex
                ));
            } else {
                NetworkManager.sendToServer(ServerboundCablePacket.id(), ServerboundCablePacket.make(
                        this.getBlockPos(),
                        target,
                        this.insertingCableIndex,
                        portIndex
                ));
            }
        }

        endInsertCable();
    }

    @Environment(EnvType.CLIENT)
    public void endInsertCable() {
        this.insertingCableIsInput = false;
        this.insertingCableIndex = -1;
        this.insertingCableStart = null;
    }

    @Environment(EnvType.CLIENT)
    public static void animationTick(Level ignoredLevel, BlockPos ignoredPos, BlockState ignoredState, SynthBlockEntity blockEntity) {
        blockEntity.animationTime++;
    }

    private CompoundTag prepareUpdateTag(CompoundTag tag) {
        ListTag savedInputs = new ListTag();
        ListTag savedOutputs = new ListTag();

        savedInputs.addAll(this.inputs.stream().map(Connection::getTag).toList());
        savedOutputs.addAll(this.outputs.stream().map(Output::getTag).toList());

        tag.put("Inputs", savedInputs);
        tag.put("Outputs", savedOutputs);

        return tag;
    }

    private void updateClient() {
        if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    protected void pushUpdate() {
        this.setChanged();
        this.updateClient();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        prepareUpdateTag(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.inputs.clear();
        this.outputs.clear();

        this.inputs.addAll(tag.getList("Inputs", 10).stream().map(Connection::of).toList());
        this.outputs.addAll(tag.getList("Outputs", 10).stream().map(Output::of).toList());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return prepareUpdateTag(new CompoundTag());
    }

    public boolean tryAddInput(BlockPos pos, int from, int to) {
        if (this.level == null) return false;

        Connection input = new Connection(pos, from, to);
        if (this.inputs.contains(input)) return false;

        this.inputs.add(input);
        this.pushUpdate();
        return true;
    }

    public boolean tryAddOutput(BlockPos pos, int from, int to, int color) {
        if (this.level == null) return false;

        Output output = new Output(pos, from, to, color);
        if (this.outputs.contains(output)) return false;

        BlockEntity blockEntity = this.level.getBlockEntity(pos);
        if (blockEntity instanceof SynthBlockEntity synthBlockEntity) {
            if (!synthBlockEntity.tryAddInput(this.getBlockPos(), from, to)) return false;
        } else return false;

        this.outputs.add(output);
        this.pushUpdate();
        return true;
    }

    public void popOutput(Output output) {
        if (this.level == null) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) return;
        if (!this.outputs.contains(output)) return;

        EnumProperty<FrontAndTop> fat = BlockStateProperties.ORIENTATION;

        Vec3 from = Vec3.atLowerCornerOf(this.getBlockPos())
                .add(Vec3.atLowerCornerOf(
                        this.getBlockState().getValue(fat).front().getNormal()
                ).scale(0.75));

        BlockState outputState = level.getBlockState(output.pos);
        Vec3 to = outputState.getBlock() instanceof SynthBlock ? Vec3.atLowerCornerOf(output.pos)
                .add(Vec3.atLowerCornerOf(
                        outputState.getValue(fat).front().getNormal()
                ).scale(0.75)) : Vec3.atLowerCornerOf(output.pos);

        Vec3 center = from.add(to).scale(0.5);

        ItemEntity itemEntity = new ItemEntity(
                this.level,
                center.x + 0.5,
                center.y + 0.5,
                center.z + 0.5,
                output.getItemStack()
        );

        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    public void popInputsFromOutput(BlockPos pos) {
        this.inputs.removeIf(input -> input.pos.equals(pos));

        this.pushUpdate();
    }

    public void popOutputsFromInput(BlockPos pos) {
        this.outputs.removeIf(output -> {
            if (output.pos.equals(pos)) {
                this.popOutput(output);
                return true;
            }
            return false;
        });

        this.pushUpdate();
    }

    public void removeMatchingInputs(BlockPos pos, int to) {
        if (this.level == null) return;

        this.inputs.removeIf(input -> {
            if (input.to == to) {
                if (this.level.getBlockEntity(input.pos) instanceof SynthBlockEntity outputBlockEntity) {
                    outputBlockEntity.getOutputs().removeIf(output -> {
                        if (output.to == to && output.pos.equals(pos)) {
                            outputBlockEntity.popOutput(output);
                            return true;
                        }
                        return false;
                    });

                    outputBlockEntity.pushUpdate();
                }

                return true;
            }
            return false;
        });

        this.pushUpdate();
    }

    public void removeMatchingOutputs(BlockPos pos, int from) {
        if (this.level == null) return;

        this.outputs.removeIf(output -> {
            if (output.from == from) {
                if (level.getBlockEntity(output.pos) instanceof SynthBlockEntity inputBlockEntity) {
                    inputBlockEntity.getInputs().removeIf(
                            input -> input.from == from && input.pos.equals(pos)
                    );
                    inputBlockEntity.pushUpdate();
                }

                this.popOutput(output);
                return true;
            }
            return false;
        });

        this.pushUpdate();
    }

    public static class Connection {
        public final BlockPos pos;
        public final int from;
        public final int to;

        public Connection(BlockPos pos, int from, int to) {
            this.pos = pos;
            this.from = Math.max(from, 0);
            this.to = Math.max(to, 0);
        }

        public static Connection of(Tag tag) {
            CompoundTag compoundTag = (CompoundTag) tag;
            return new Connection(
                    BlockPos.of(compoundTag.getLong("pos")),
                    compoundTag.getInt("from"),
                    compoundTag.getInt("to")
            );
        }

        public CompoundTag getTag() {
            CompoundTag tag = new CompoundTag();
            tag.putLong("pos", this.pos.asLong());
            tag.putInt("from", this.from);
            tag.putInt("to", this.to);
            return tag;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof Connection connection) {
                return this.pos.equals(connection.pos) && this.from == connection.from && this.to == connection.to;
            }
            return false;
        }
    }

    public static class Output extends Connection {
        public final int color;

        @Environment(EnvType.CLIENT) private static final double BEZIER_SCALE = 0.25;

        @Environment(EnvType.CLIENT) public Vec3 cableStart;
        @Environment(EnvType.CLIENT) public Vec3 cableEnd;
        @Environment(EnvType.CLIENT) public Vec3 cableStartNorm;
        @Environment(EnvType.CLIENT) public Vec3 cableEndNorm;
        @Environment(EnvType.CLIENT) public Vector3f[] bezierPoints;
        @Environment(EnvType.CLIENT) public Vector3f[] bezierNormals;
        @Environment(EnvType.CLIENT) public Vector3f[] bezierUps;

        public Output(BlockPos pos, int from, int to, int color) {
            super(pos, from, to);
            this.color = color;
        }

        public static Output of(Tag tag) {
            CompoundTag compoundTag = (CompoundTag) tag;
            return new Output(
                    BlockPos.of(compoundTag.getLong("pos")),
                    compoundTag.getInt("from"),
                    compoundTag.getInt("to"),
                    compoundTag.getInt("color")
            );
        }

        @Override
        public CompoundTag getTag() {
            CompoundTag tag = super.getTag();
            tag.putInt("color", this.color);
            return tag;
        }

        public ItemStack getItemStack() {
            ItemStack stack = new ItemStack(ModItems.get("patch_cable"));
            if (this.color == 0xffffff) return stack;

            stack.getOrCreateTagElement("display").putInt("color", this.color);
            return stack;
        }

        @Environment(EnvType.CLIENT)
        private boolean ensureCablePositions(SynthBlockEntity synthBlockEntity) {
            if (this.cableStart != null && this.cableEnd != null) return true;

            if (synthBlockEntity.level == null) return false;

            Optional<SynthBlockEntity> otherBlockEntity = synthBlockEntity.level.getBlockEntity(this.pos, ModBlockEntityTypes.SYNTH.get());
            if (otherBlockEntity.isEmpty()) return false;

            BlockState synthBlockState = synthBlockEntity.getBlockState();
            SynthBlock synthBlock = (SynthBlock) synthBlockState.getBlock();
            FrontAndTop fat = synthBlockState.getValue(BlockStateProperties.ORIENTATION);

            BlockState otherBlockState = otherBlockEntity.get().getBlockState();
            SynthBlock otherBlock = (SynthBlock) otherBlockState.getBlock();
            FrontAndTop otherFat = otherBlockState.getValue(BlockStateProperties.ORIENTATION);

            try {
                this.cableStart = SynthBlock.face3dPosition(
                        synthBlock.getOutputPositions()[this.from], fat, -0.03125
                );

                this.cableEnd = SynthBlock.face3dPosition(
                        otherBlock.getInputPositions()[this.to], otherFat, -0.03125
                ).add(Vec3.atLowerCornerOf(this.pos.subtract(synthBlockEntity.getBlockPos())));
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }

            this.cableStartNorm = Vec3.atLowerCornerOf(fat.front().getNormal());
            this.cableEndNorm = Vec3.atLowerCornerOf(otherFat.front().getNormal());

            return true;
        }

        @Environment(EnvType.CLIENT)
        private static Vector3f cubicBezier(float delta, Vec3[] points) {
            Vec3 first = points[1].subtract(points[0]).scale(delta * 3);
            Vec3 second = points[2].subtract(points[1].scale(2)).add(points[0]).scale(delta * delta * 3);
            Vec3 third = points[3].subtract(points[0]).add(points[1].subtract(points[2]).scale(3)).scale(delta * delta * delta);
            return points[0].add(first).add(second).add(third).toVector3f();
        }

        @Environment(EnvType.CLIENT)
        private static Vector3f cubicBezierDerivative(float delta, Vec3[] points) {
            Vec3 first = points[1].subtract(points[0]).scale(3);
            Vec3 second = points[2].subtract(points[1].scale(2)).add(points[0]).scale(delta * 6);
            Vec3 third = points[3].subtract(points[0]).add(points[1].subtract(points[2]).scale(3)).scale(delta * delta * 3);
            return first.add(second).add(third).normalize().toVector3f();
        }

        @Environment(EnvType.CLIENT)
        public boolean ensureBezier(SynthBlockEntity synthBlockEntity) {
            if (this.bezierPoints != null) return true;

            if (!this.ensureCablePositions(synthBlockEntity)) return false;
            
            double length = this.cableStart.subtract(this.cableEnd).length();
            int pointCount = (int) (length * 16) + 2;

            this.bezierPoints = new Vector3f[pointCount];
            this.bezierNormals = new Vector3f[pointCount];
            this.bezierUps = new Vector3f[pointCount];

            double usedScale = BEZIER_SCALE * (
                    1 - this.cableStartNorm.dot(this.cableEndNorm) +
                    4 * (1 - Math.exp(-length / 4))
            );

            Vec3[] controlPoints = new Vec3[]{
                    this.cableStart,
                    this.cableStart.add(this.cableStartNorm.scale(usedScale)),
                    this.cableEnd.add(this.cableEndNorm.scale(usedScale)),
                    this.cableEnd
            };

            // init values
            for (int i = 0; i < pointCount; i++) {
                this.bezierPoints[i] = cubicBezier((float) i / (pointCount - 1), controlPoints);
                this.bezierUps[i] = cubicBezierDerivative((float) i / (pointCount - 1), controlPoints);
                this.bezierNormals[i] = new Vector3f(this.bezierUps[i].z, 0, this.bezierUps[i].x).orthogonalize(this.bezierUps[i]);

                if (this.bezierNormals[i].y < 0) this.bezierNormals[i].rotateAxis(Mth.PI, this.bezierUps[i].x, this.bezierUps[i].y, this.bezierUps[i].z);
            }

            // blur normals
            for (int b = 0; b < 4; b++) {
                for (int i = 0; i < pointCount - 1; i++) {
                    this.bezierNormals[i].lerp(this.bezierNormals[i + 1], 0.5F);
                }

                for (int i = pointCount - 1; i > 0; i--) {
                    this.bezierNormals[i].lerp(this.bezierNormals[i - 1], 0.5F);
                }
            }

            // finalize values
            for (int i = 0; i < pointCount; i++) {
                this.bezierNormals[i].normalize();
                this.bezierUps[i].cross(this.bezierNormals[i]);
                this.bezierNormals[i].mul(SynthBlock.CABLE_SIZE / 2);
                this.bezierUps[i].mul(SynthBlock.CABLE_SIZE / 2);
            }

            return true;
        }
    }
}
