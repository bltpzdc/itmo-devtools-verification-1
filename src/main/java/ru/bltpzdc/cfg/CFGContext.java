package ru.bltpzdc.cfg;

import java.util.Map;
import java.util.Optional;

public class CFGContext {
    private CFGNode currentNode;
    private final CFGNode followingNode;
    private final Map<String, CFGNode> labels;
    private final Optional<CFGNode> afterBreakNode;

    public CFGContext(CFGNode followingNode, Map<String, CFGNode> labels, Optional<CFGNode> afterBreakNode) {
        this.currentNode = followingNode;
        this.followingNode = followingNode;
        this.labels = labels;
        this.afterBreakNode = afterBreakNode;
    }

    public Map<String, CFGNode> getLabels() {
        return labels;
    }

    public CFGNode getCurrentNode() {
        return currentNode;
    }
    
    public CFGNode getFollowingNode() {
        return followingNode;
    }

    public void setCurrentNode(CFGNode currentNode) {
        this.currentNode = currentNode;
    }

    public Optional<CFGNode> getAfterBreakNode() {
        return afterBreakNode;
    }
}
