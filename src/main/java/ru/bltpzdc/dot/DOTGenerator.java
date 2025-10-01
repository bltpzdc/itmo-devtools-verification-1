package ru.bltpzdc.dot;

import java.util.Map;
import java.util.stream.Collectors;

import ru.bltpzdc.cfg.CFGNode;
import ru.bltpzdc.cfg.CFGNodeType;

public class DOTGenerator {
    public static String convert(Map<String, CFGNode> nodes) {
        var dotNodes = nodes.values().stream()
            .filter(node -> node.getType() != CFGNodeType.LABEL)
            .map(node -> toDot(node))
            .distinct()
            .collect(Collectors.joining("\n"));

        var dotEdges = nodes.values().stream()
            .filter(node -> node.getType() != CFGNodeType.LABEL)
            .flatMap(node -> node.getSuccessors().stream()
                .map(suc -> toDot(node, suc.b, suc.a))
            )
            .distinct()
            .collect(Collectors.joining("\n"));

        return new StringBuilder()
            .append(PREFIX)
            .append(dotNodes)
            .append("\n\n")
            .append(dotEdges)
            .append(SUFFIX)
            .toString();
    }

    private final static String PREFIX = """
                    digraph CFG {
                        rankdir=TB;
                        node [shape=rectangle, fontname="Arial"];

                    """;
    private final static String SUFFIX = "\n}";

    private static final Map<CFGNodeType, String> nodeShapes = Map.of(
        CFGNodeType.ENTRY, "ellipse",
        CFGNodeType.EXIT, "ellipse",
        CFGNodeType.COND, "diamond",
        CFGNodeType.EXPR, "rectangle"
    );

    private static final Map<String, String> edgeColors = Map.of(
        "True", "green",
        "False", "red"
    );

    // if CFGNodeType is LABEL, we need to ignore this node
    private static String processCFGNodeId(CFGNode node) {
        return node.getType() == CFGNodeType.LABEL
             ? node.getSuccessors().get(0).b.getId()
             : node.getId();
    }

    private static String getEdgeColor(String label) {
        return edgeColors.containsKey(label)
             ? edgeColors.get(label)
             : "black";
    }

    private static String toDot(CFGNode node) {
        return String.format("\t\"%s\" [label=\"%s\", shape=%s];",
                             node.getId(),
                             node.getLabel(),
                             nodeShapes.get(node.getType()));
    }

    private static String toDot(CFGNode node1, CFGNode node2, String label) {
        return String.format("\t\"%s\" -> \"%s\" [label=\"%s\", color=%s]",
                             processCFGNodeId(node1),
                             processCFGNodeId(node2),
                             label,
                             getEdgeColor(label));
    }
}
