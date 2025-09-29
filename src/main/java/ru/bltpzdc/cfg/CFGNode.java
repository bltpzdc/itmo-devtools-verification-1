package ru.bltpzdc.cfg;

import java.util.ArrayList;
import java.util.List;

public class CFGNode {
    private boolean  was = false;
    private final String id;
    private final String label;
    private final NodeType type;
    private final List<CFGNode> successors;

    public CFGNode(String id, String label, NodeType type) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.successors = new ArrayList<>();
    }

    public void addSuccessor(CFGNode node) {
        successors.add(node);
    }

    @Override
    public String toString() {
        if ( !was ) {
            was = true;
            return "CFGNode(id: " + id + ", label: " + label + ", successors: " + successors.toString() + ")";
        } else {
            return "CFGNode(" + id +")";
        }
    }
}

enum NodeType {
    ENTRY, EXIT, STMT, COND, LABEL,
}
