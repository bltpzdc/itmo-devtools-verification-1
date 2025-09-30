package ru.bltpzdc.cfg;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.utils.Pair;

public class CFGNode {
    private boolean  was = false;
    private final String id;
    private final String label;
    private final CFGNodeType type;
    private final List<Pair<String, CFGNode>> successors;

    public CFGNode(String id, String label, CFGNodeType type) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.successors = new ArrayList<>();
    }

    public void addSuccessor(CFGNode node) {
        successors.add(new Pair<>("", node));
    }

    public void addSuccessor(CFGNode node, String condition) {
        successors.add(new Pair<>(condition, node));
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

    public CFGNodeType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public List<Pair<String, CFGNode>> getSuccessors() {
        return successors;
    }
}
