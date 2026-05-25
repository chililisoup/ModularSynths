package dev.chililisoup.modularsynths.synthesis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.AbstractSynthBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractSynth {
    public static final Codec<List<InPort>> INPUTS_CODEC = Codec.list(InPort.CODEC);
    public static final Codec<List<OutPort>> OUTPUTS_CODEC = Codec.list(OutPort.CODEC);

    public final SynthBlockEntity synthBlockEntity;
    protected final InPort[] inputs;
    protected final OutPort[] outputs;

    public AbstractSynth(SynthBlockEntity synthBlockEntity) {
        int inputs;
        int outputs;
        if (synthBlockEntity.getBlockState().getBlock() instanceof AbstractSynthBlock<?> synthBlock) {
            inputs = synthBlock.inputPositions().length;
            outputs = synthBlock.outputPositions().length;
        } else {
            inputs = 0;
            outputs = 0;
        }

        this.synthBlockEntity = synthBlockEntity;
        this.inputs = new InPort[inputs];
        for (int i = 0; i < inputs; i++) this.inputs[i] = new InPort(new ArrayList<>());
        this.outputs = new OutPort[outputs];
        for (int i = 0; i < outputs; i++) this.outputs[i] = new OutPort(new ArrayList<>());
    }

    public void stop() {}

    public int[] dependenciesFor(int outPort) {
        return new int[0];
    }

    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return InputSampleSource::get;
    }

    public @Nullable Runnable bufferCleanupTask() {
        return null;
    }

    public List<InPort> getInputList() {
        return List.of(this.inputs);
    }

    public void loadInputs(List<InPort> ports) {
        int max = Math.min(ports.size(), this.inputs.length);
        for (int i = 0; i < max; i++) this.inputs[i] = ports.get(i);
    }

    public List<OutPort> getOutputList() {
        return List.of(this.outputs);
    }

    public void loadOutputs(List<OutPort> ports) {
        int max = Math.min(ports.size(), this.outputs.length);
        for (int i = 0; i < max; i++) this.outputs[i] = ports.get(i);
    }

    public void load(ValueInput input) {
        this.loadInputs(input.read("inputs", AbstractSynth.INPUTS_CODEC).orElse(List.of()));
        this.loadOutputs(input.read("outputs", AbstractSynth.OUTPUTS_CODEC).orElse(List.of()));
    }

    public void save(ValueOutput output) {
        if (this.inputs.length > 0)
            output.store("inputs", AbstractSynth.INPUTS_CODEC, this.getInputList());
        if (this.outputs.length > 0)
            output.store("outputs", AbstractSynth.OUTPUTS_CODEC, this.getOutputList());
    }

    public void afterLoad(Level level) {
        for (InPort port : this.inputs)
            port.connections.forEach(connection -> connection.updateSynth(level));
    }

    public void powerOff() {}

    public boolean addInput(SynthInputConnection connection, int inPort) {
        if (this.inputs.length <= inPort || inPort < 0) return false;

        Level level = this.synthBlockEntity.getLevel();
        if (level == null) return false;

        InPort port = this.inputs[inPort];
        if (port.connections.contains(connection)) return false;

        connection.updateSynth(level);
        AbstractSynth inputSynth = connection.synth();
        if (inputSynth == null) return false;

        if (!inputSynth.addOutput(
                new SynthOutputConnection(this.synthBlockEntity.getBlockPos(), inPort),
                connection.outPort()
        )) return false;

        port.connections.add(connection);
        this.synthBlockEntity.setChanged(GameEvent.ITEM_INTERACT_FINISH);
        this.updateConnectedSpeaker();
        return true;
    }

    private boolean addOutput(SynthOutputConnection connection, int outPort) {
        if (this.outputs.length <= outPort || outPort < 0) return false;

        OutPort port = this.outputs[outPort];
        if (port.connections.contains(connection)) return true;

        port.connections.add(connection);
        this.synthBlockEntity.setChanged(GameEvent.ITEM_INTERACT_FINISH);
        return true;
    }

    public @Nullable SynthInputConnection popInput(BlockPos pos, int inPort, int outPort) {
        if (this.inputs.length <= inPort || inPort < 0) return null;

        for (SynthInputConnection connection : this.inputs[inPort].connections) {
            if (connection.outPort() != outPort) continue;
            if (!connection.pos().equals(pos)) continue;

            this.inputs[inPort].connections.remove(connection);
            this.synthBlockEntity.setChanged();
            this.updateConnectedSpeaker();
            return connection;
        }

        return null;
    }

    public void removeOutput(BlockPos pos, int inPort, int outPort) {
        if (this.outputs.length <= outPort || outPort < 0) return;

        for (SynthOutputConnection connection : this.outputs[outPort].connections) {
            if (connection.inPort() != inPort) continue;
            if (!connection.pos().equals(pos)) continue;

            this.outputs[outPort].connections.remove(connection);
            this.synthBlockEntity.setChanged();
            return;
        }
    }

    public void popInputs(int inPort, Direction face) {
        if (this.inputs.length <= inPort || inPort < 0) return;

        Level level = this.synthBlockEntity.getLevel();
        BlockPos pos = this.synthBlockEntity.getBlockPos();
        ArrayList<SynthInputConnection> toRemove = new ArrayList<>(this.inputs[inPort].connections);
        this.inputs[inPort].connections.clear();

        if (level != null) for (SynthInputConnection connection : toRemove) {
            Block.popResourceFromFace(level, pos, face, connection.getItem());
            if (level.getBlockEntity(connection.pos()) instanceof SynthBlockEntity blockEntity)
                blockEntity.synth.removeOutput(pos, inPort, connection.outPort());
        }

        this.synthBlockEntity.setChanged();
        this.updateConnectedSpeaker();
    }

    public void popOutputs(int outPort, Direction face) {
        if (this.outputs.length <= outPort || outPort < 0) return;

        Level level = this.synthBlockEntity.getLevel();
        BlockPos pos = this.synthBlockEntity.getBlockPos();
        ArrayList<SynthOutputConnection> toRemove = new ArrayList<>(this.outputs[outPort].connections);
        this.outputs[outPort].connections.clear();

        if (level != null) for (SynthOutputConnection connection : toRemove) {
            if (!(level.getBlockEntity(connection.pos()) instanceof SynthBlockEntity blockEntity))
                continue;

            SynthInputConnection inputConnection = blockEntity.synth.popInput(pos, connection.inPort(), outPort);
            if (inputConnection != null) Block.popResourceFromFace(level, pos, face, inputConnection.getItem());
        }

        this.synthBlockEntity.setChanged();
    }

    public void updateConnectedSpeaker() {
        this.updateConnectedSpeaker(new HashSet<>(), 0);
    }

    public boolean updateConnectedSpeaker(HashSet<BlockPos> checked, int depth) {
        Level level = this.synthBlockEntity.getLevel();
        if (level == null) return false;

        ArrayList<AbstractSynth> toUpdate = new ArrayList<>();
        for (OutPort port : this.outputs) for (SynthOutputConnection connection : port.connections) {
            if (checked.contains(connection.pos())) continue;
            checked.add(connection.pos());

            if (!(level.getBlockEntity(connection.pos()) instanceof SynthBlockEntity blockEntity))
                continue;

            if (blockEntity.synth instanceof SynthSpeaker) {
                blockEntity.setChanged();
                return true;
            }

            if (depth < ModularSynths.MAX_SEARCH_DEPTH) toUpdate.add(blockEntity.synth);
        }

        for (AbstractSynth synth : toUpdate) if (
                synth.updateConnectedSpeaker(checked, depth + 1)
        ) return true;

        return false;
    }

    public boolean inputEmpty(int inPort) {
        if (this.inputs.length <= inPort || inPort < 0) return true;
        return this.inputs[inPort].connections.isEmpty();
    }

    public boolean outputEmpty(int outPort) {
        if (this.outputs.length <= outPort || outPort < 0) return true;
        return this.outputs[outPort].connections.isEmpty();
    }

    public record InPort(ArrayList<SynthInputConnection> connections) {
        public static final Codec<InPort> CODEC = Codec.list(
                SynthInputConnection.CODEC.codec()
        ).comapFlatMap(input -> DataResult.success(new InPort(input)), InPort::connections);

        public InPort(List<SynthInputConnection> connections) {
            this(new ArrayList<>(connections));
        }
    }

    public record OutPort(ArrayList<SynthOutputConnection> connections) {
        public static final Codec<OutPort> CODEC = Codec.list(
                SynthOutputConnection.CODEC.codec()
        ).comapFlatMap(input -> DataResult.success(new OutPort(input)), OutPort::connections);

        public OutPort(List<SynthOutputConnection> connections) {
            this(new ArrayList<>(connections));
        }
    }
}
