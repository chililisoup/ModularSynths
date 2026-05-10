package dev.chililisoup.modularsynths.synthesis;

import dev.chililisoup.modularsynths.ModularSynths;

import java.util.*;

public final class SynthGraph {
    private final Node outNode;

    public SynthGraph(SynthSpeaker output) {
        HashMap<Integer, Node> nodes = new HashMap<>();

        this.outNode = new Node(new NodeDependency[]{
                NodeDependency.of(nodes, output, 0, 0)
        }, output.processorFor(0));
    }

    public short[] process(int size) {
        PolySampleSource processedNode = processNode(new HashMap<>(), this.outNode, size, 0);

        double[] samples = null;
        if (processedNode != null) samples = processedNode.monoSamples(size);
        if (samples == null) samples = new double[size];

        short[] processed = new short[size];
        for (int i = 0; i < size; i++) processed[i] = (short) (samples[i] * Short.MAX_VALUE);
        return processed;
    }

    private static PolySampleSource processNode(HashMap<Node, PolySampleSource> processed, Node node, int size, int depth) {
        if (processed.containsKey(node)) return processed.get(node);

        PolySampleSource[] inputs = new PolySampleSource[node.dependencies.length];
        if (depth <= ModularSynths.MAX_SEARCH_DEPTH) for (int i = 0; i < node.dependencies.length; i++) {
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

    private static Set<Node> gatherNodes(HashMap<Integer, Node> nodes, List<SynthInputConnection> inputs, int depth) {
        ArrayList<Node> gathered = new ArrayList<>();

        for (SynthInputConnection connection : inputs) {
            AbstractSynth synth = connection.synth();
            ModularSynths.LOGGER.info("Processing connection {}", connection.synth());
            if (synth == null) continue;
            int outPort = connection.outPort();
            int nodeHash = getNodeHash(synth, outPort);
            ModularSynths.LOGGER.info("Connection validated. Port {}, ID {}", outPort, nodeHash);
            if (nodes.containsKey(nodeHash)) {
                ModularSynths.LOGGER.info("ID already processed. Linking...");
                gathered.add(nodes.get(nodeHash));
                continue;
            }

            if (depth > ModularSynths.MAX_SEARCH_DEPTH) continue;

            int[] requiredPorts = synth.dependenciesFor(outPort);
            ModularSynths.LOGGER.info("Gathering {} depended upon ports", requiredPorts.length);
            NodeDependency[] dependencies = new NodeDependency[requiredPorts.length];
            for (int port : requiredPorts) dependencies[port] = NodeDependency.of(
                    nodes, synth, port, depth + 1
            );

            Node node = new Node(dependencies, synth.processorFor(outPort));
            nodes.put(nodeHash, node);
            gathered.add(node);
            ModularSynths.LOGGER.info("Node {} fully gathered.", nodeHash);
        }

        return Set.copyOf(gathered);
    }

    private static List<SynthInputConnection> collapseInputs(AbstractSynth synth, int port) {
        ArrayList<SynthInputConnection> inputs = new ArrayList<>();

        for (SynthInputConnection connection : synth.inputs[port].connections()) {
            if (connection.synth() instanceof SynthRelay relay)
                collapseRelay(inputs, relay, connection.outPort(), 0);
            else inputs.add(connection);
        }

        return inputs;
    }

    private static void collapseRelay(ArrayList<SynthInputConnection> inputs, SynthRelay relay, int outPort, int depth) {
        for (SynthInputConnection connection : relay.inputs[outPort].connections()) {
            if (depth < ModularSynths.MAX_SEARCH_DEPTH && connection.synth() instanceof SynthRelay nextRelay)
                collapseRelay(inputs, nextRelay, connection.outPort(), depth + 1);
            else inputs.add(connection);
        }
    }

    private record Node(NodeDependency[] dependencies, NodeProcessor processor) {}

    private record NodeDependency(Set<Node> nodes) {
        private static NodeDependency of(HashMap<Integer, Node> nodes, AbstractSynth synth, int port, int depth) {
            ModularSynths.LOGGER.info("Gathering nodes for {}", synth);
            return new NodeDependency(gatherNodes(nodes, collapseInputs(synth, port), depth));
        }
    }

    @FunctionalInterface
    public interface NodeProcessor {
        PolySampleSource process(InputSampleSource inputs, int size);
    }
}
