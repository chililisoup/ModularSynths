package dev.chililisoup.modularsynths.synthesis;

import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;
import static dev.chililisoup.modularsynths.ModularSynths.IS_DEV;
import static dev.chililisoup.modularsynths.ModularSynths.MAX_SEARCH_DEPTH;

public final class SynthGraph {
    private final Node outNode;
    private final List<Runnable> bufferCleanupTasks;

    public SynthGraph(SynthSpeaker output) {
        HashMap<Integer, Node> nodes = new HashMap<>();
        HashMap<Integer, Runnable> cleanupTasks = new HashMap<>();

        this.outNode = new Node(new NodeDependency[]{
                NodeDependency.of(nodes, cleanupTasks, output, 0, 0)
        }, output.processorFor(0));
        this.bufferCleanupTasks = cleanupTasks.values().stream().filter(Objects::nonNull).toList();
        if (IS_DEV) LOGGER.info("Built graph with {} cleanup task(s)", this.bufferCleanupTasks.size());
    }

    public short[] process(int size) {
        PolySampleSource processedNode = processNode(new HashMap<>(), this.outNode, size, 0);

        double[] samples = null;
        if (processedNode != null) samples = processedNode.monoSamples(size);
        if (samples == null) samples = new double[size];

        short[] processed = new short[size];
        for (int i = 0; i < size; i++) processed[i] = (short) (samples[i] * Short.MAX_VALUE);

        this.bufferCleanupTasks.forEach(Runnable::run);
        return processed;
    }

    private static PolySampleSource processNode(HashMap<Node, PolySampleSource> processed, Node node, int size, int depth) {
        if (processed.containsKey(node)) return processed.get(node);

        PolySampleSource[] inputs = new PolySampleSource[node.dependencies.length];
        if (depth <= MAX_SEARCH_DEPTH) for (int i = 0; i < node.dependencies.length; i++) {
            List<Node> inputNodes = List.copyOf(node.dependencies[i].nodes);
            if (inputNodes.isEmpty()) {
                inputs[i] = new PolySampleSource(new double[size]);
                continue;
            }

            if (inputNodes.size() == 1) {
                inputs[i] = processNode(processed, inputNodes.getFirst(), size, depth + 1);
                continue;
            }

            inputs[i] = PolySampleSource.merged(size, inputNodes.stream()
                    .map(inputNode -> processNode(processed, inputNode, size, depth))
                    .toArray(PolySampleSource[]::new)
            );
        } else for (int i = 0; i < node.dependencies.length; i++)
            inputs[i] = new PolySampleSource(new double[size]);

        PolySampleSource processedNode = node.processor.process(new InputSampleSource(inputs), size);
        processed.put(node, processedNode);
        return processedNode;
    }

    private static int getNodeHash(AbstractSynth synth, int outPort) {
        return Objects.hash(synth, outPort);
    }

    private static Set<Node> gatherNodes(
            HashMap<Integer, Node> nodes, HashMap<Integer, Runnable> cleanupTasks, List<SynthInputConnection> inputs, int depth
    ) {
        ArrayList<Node> gathered = new ArrayList<>();

        for (SynthInputConnection connection : inputs) {
            AbstractSynth synth = connection.synth();
            if (IS_DEV) LOGGER.info("Processing connection {}", connection.synth());
            if (synth == null) continue;
            int outPort = connection.outPort();
            int nodeHash = getNodeHash(synth, outPort);
            if (IS_DEV) LOGGER.info("Connection validated. Port {}, ID {}", outPort, nodeHash);
            if (nodes.containsKey(nodeHash)) {
                if (IS_DEV) LOGGER.info("ID already processed. Linking...");
                gathered.add(nodes.get(nodeHash));
                continue;
            }

            int taskKey = synth.hashCode();
            if (!cleanupTasks.containsKey(taskKey))
                cleanupTasks.put(taskKey, synth.bufferCleanupTask());

            if (depth > MAX_SEARCH_DEPTH) continue;

            int[] requiredPorts = synth.dependenciesFor(outPort);
            if (IS_DEV) LOGGER.info("Gathering {} depended upon ports", requiredPorts.length);
            NodeDependency[] dependencies = new NodeDependency[requiredPorts.length];
            for (int i = 0; i < requiredPorts.length; i++) {
                dependencies[i] = NodeDependency.of(
                        nodes, cleanupTasks, synth, requiredPorts[i], depth + 1
                );
            }

            Node node = new Node(dependencies, synth.processorFor(outPort));
            nodes.put(nodeHash, node);
            gathered.add(node);
            if (IS_DEV) LOGGER.info("Node {} fully gathered.", nodeHash);
        }

        return Set.copyOf(gathered);
    }

    private static List<SynthInputConnection> collapseInputs(AbstractSynth synth, int port) {
        ArrayList<SynthInputConnection> inputs = new ArrayList<>();

        for (SynthInputConnection connection : synth.inputs[port].connections()) {
            if (connection.synth() instanceof CableRelay relay)
                collapseRelay(inputs, relay, connection.outPort(), 0);
            else inputs.add(connection);
        }

        return inputs;
    }

    private static void collapseRelay(ArrayList<SynthInputConnection> inputs, CableRelay relay, int outPort, int depth) {
        for (SynthInputConnection connection : relay.inputs[outPort].connections()) {
            if (depth < MAX_SEARCH_DEPTH && connection.synth() instanceof CableRelay nextRelay)
                collapseRelay(inputs, nextRelay, connection.outPort(), depth + 1);
            else inputs.add(connection);
        }
    }

    private record Node(NodeDependency[] dependencies, NodeProcessor processor) {}

    private record NodeDependency(Set<Node> nodes) {
        private static NodeDependency of(
                HashMap<Integer, Node> nodes, HashMap<Integer, Runnable> cleanupTasks, AbstractSynth synth, int port, int depth
        ) {
            if (IS_DEV) LOGGER.info("Gathering nodes for {}", synth);
            return new NodeDependency(gatherNodes(nodes, cleanupTasks, collapseInputs(synth, port), depth));
        }
    }

    @FunctionalInterface
    public interface NodeProcessor {
        PolySampleSource process(InputSampleSource inputs, int size);
    }
}
