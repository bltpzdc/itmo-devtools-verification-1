package ru.bltpzdc.dot;

import java.util.Map;
import java.util.Optional;

import ru.bltpzdc.cfg.CFGNode;
import ru.bltpzdc.cfg.CFGNodeType;

public class DOTGenerator {
    public static Optional<String> convert(String methodName, Map<String, CFGNode> nodes) {
        Optional<String> result;
        try {
            var dot = new StringBuilder();
            dot.append("""
                    digraph CFG {
                        rankdir=TB;
                        node [shape=rectangle, fontname="Arial"];


                    """);

            for ( var node : nodes.values() ) {
                var type = node.getType();
                if ( type != CFGNodeType.LABEL ) {
                    var shape = nodeShapes.get(node.getType());
                    dot.append(String.format("    \"%s\" [label=\"%s\", shape=%s];\n",
                        node.getId(), node.getLabel(), shape));
                }
            }

            dot.append("\n");

            for ( var node : nodes.values() ) {
                if ( node.getType() != CFGNodeType.LABEL ) {
                    for ( var successor : node.getSuccessors() ) {
                        dot.append(String.format("    \"%s\" -> \"%s\" [label=\"%s\", color=%s]\n",
                            node.getId(), processCFGNodeId(successor.b), successor.a, getEdgeColor(successor.a)));
                    }
                }
            }

            dot.append("}");
            result = Optional.of(dot.toString());
        } catch (Exception e) {
            result = Optional.empty();
        }

        return result;
    }

    // if CFGNodeType is LABEL, we need to ignore this node
    private static String processCFGNodeId(CFGNode node) {
        return node.getType() == CFGNodeType.LABEL
             ? node.getSuccessors().get(0).b.getId()
             : node.getId();
    }

    private static final Map<CFGNodeType, String> nodeShapes = Map.of(
        CFGNodeType.ENTRY, "ellipse",
        CFGNodeType.EXIT, "ellipse",
        CFGNodeType.COND, "diamond",
        CFGNodeType.STMT, "rectangle"
    );

    private static final Map<String, String> edgeColors = Map.of(
        "True", "green",
        "False", "red"
    );

    private static String getEdgeColor(String label) {
        return edgeColors.containsKey(label)
             ? edgeColors.get(label)
             : "black";
    }
}
