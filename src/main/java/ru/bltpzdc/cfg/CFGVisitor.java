package ru.bltpzdc.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class CFGVisitor extends VoidVisitorAdapter<CFGContext> {
    private final Map<String, CFGNode> nodes;
    private static long nodeIDGenerator = 0;

    public CFGVisitor() {
        this.nodes = new HashMap<>();
    }

    public Map<String, CFGNode> getNodes() {
        return nodes;
    }

    public CFGNode createNode(String label, CFGNodeType type) {
        var id = "node_" + nodeIDGenerator++;
        var node = new CFGNode(id, label, type);
        nodes.put(id, node);

        return node;
    }

    @Override
    public void visit(LabeledStmt labeledStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();

        var label = labeledStmt.getLabel().toString();
        var labelNode = createNode(label, CFGNodeType.LABEL);

        var labeledCtx = new CFGContext(followingNode, ctx.getLabels(), ctx.getAfterBreakNode(), ctx.getExitNode());
        labeledCtx.getLabels().put(label, labelNode);

        labeledStmt.getStatement().accept(this, labeledCtx);
        labelNode.addSuccessor(labeledCtx.getCurrentNode());

        ctx.setCurrentNode(labelNode);
    }

    @Override
    public void visit(BreakStmt breakStmt, CFGContext ctx) {
        var optLabel = breakStmt.getLabel();

        var followingNode = ctx.getFollowingNode();
        if ( optLabel.isPresent() && ctx.getLabels().containsKey(optLabel.get().toString()) ) {
            followingNode = ctx.getLabels().get(optLabel.get().toString());
        } else if ( ctx.getAfterBreakNode().isPresent() ) {
            followingNode = ctx.getAfterBreakNode().get();
        }

        ctx.setCurrentNode(followingNode);
    }

    @Override
    public void visit(BlockStmt blockStmt, CFGContext ctx) {
        var currentNode = ctx.getFollowingNode();
        var stmts = blockStmt.getStatements();

        for ( int i = stmts.size() - 1; i >= 0; --i ) {
            var stmtCtx = new CFGContext(currentNode, ctx.getLabels(), ctx.getAfterBreakNode(), ctx.getExitNode());
            stmts.get(i).accept(this, stmtCtx);
            currentNode = stmtCtx.getCurrentNode();
        }

        ctx.setCurrentNode(currentNode);
    }

    @Override
    public void visit(ExpressionStmt exprStmt, CFGContext ctx) {
        var expr = exprStmt.toString();
        var stmtNode = createNode(expr, CFGNodeType.STMT);
        stmtNode.addSuccessor(ctx.getFollowingNode());
        ctx.setCurrentNode(stmtNode);
    }

    @Override
    public void visit(IfStmt ifStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();
        var condNode = createNode(ifStmt.getCondition().toString(), CFGNodeType.COND);

        var thenCtx = new CFGContext(followingNode, ctx.getLabels(), ctx.getAfterBreakNode(), ctx.getExitNode());
        ifStmt.getThenStmt().accept(this, thenCtx);

        var elseNode = followingNode;
        if ( ifStmt.hasElseBranch() ) {
            var elseCtx = new CFGContext(followingNode, ctx.getLabels(), ctx.getAfterBreakNode(), ctx.getExitNode());
            ifStmt.getElseStmt().get().accept(this, elseCtx);
            elseNode = elseCtx.getCurrentNode();
        }

        condNode.addSuccessor(thenCtx.getCurrentNode(), "True");
        condNode.addSuccessor(elseNode, "False");

        ctx.setCurrentNode(condNode);
    }
    
    @Override
    public void visit(WhileStmt whileStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();
        var condNode = createNode(whileStmt.getCondition().toString(), CFGNodeType.COND);

        var bodyCtx = new CFGContext(condNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode());
        whileStmt.getBody().accept(this, bodyCtx);

        condNode.addSuccessor(bodyCtx.getCurrentNode(), "True");
        condNode.addSuccessor(followingNode, "False");

        ctx.setCurrentNode(condNode);
    }

    @Override
    public void visit(DoStmt doStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();
        var condNode = createNode(doStmt.getCondition().toString(), CFGNodeType.COND);

        var bodyCtx = new CFGContext(condNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode());
        doStmt.getBody().accept(this, bodyCtx);

        condNode.addSuccessor(bodyCtx.getCurrentNode(), "True");
        condNode.addSuccessor(followingNode, "False");

        ctx.setCurrentNode(bodyCtx.getCurrentNode());
    }

    @Override
    public void visit(ForStmt forStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();

        var cond = forStmt.getCompare().map(expr -> expr.toString()).orElse("true");
        var condNode = createNode(cond, CFGNodeType.COND);
        
        var initNode = condNode;
        var init = forStmt.getInitialization().toString();
        if ( !init.equals("[]") ) {
            initNode = createNode(init, CFGNodeType.STMT);
            initNode.addSuccessor(condNode);
        }

        var update = forStmt.getUpdate().toString();
        var updateNode = condNode;
        if ( !update.equals("[]") ) {
            updateNode = createNode(update, CFGNodeType.STMT);
            updateNode.addSuccessor(condNode);
        }

        var bodyCtx = new CFGContext(updateNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode());
        forStmt.getBody().accept(this, bodyCtx);

        condNode.addSuccessor(bodyCtx.getCurrentNode(), "True");
        condNode.addSuccessor(followingNode, "False");

        ctx.setCurrentNode(initNode);
    }

    @Override
    public void visit(SwitchStmt switchStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();
        var switchNode = createNode("switch " + switchStmt.getSelector().toString(), CFGNodeType.COND);
        switchNode.addSuccessor(followingNode);


        var swicthEntries = switchStmt.getEntries();
        var currentNode = followingNode;
        for ( var i = swicthEntries.size() - 1; i >= 0; --i ) {
            var entryCtx = new CFGContext(currentNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode());
            var entry = swicthEntries.get(i);
            entry.accept(this, entryCtx);
            currentNode = entryCtx.getCurrentNode();
            switchNode.addSuccessor(currentNode, getSwitchEntryName(entry));
        }

        ctx.setCurrentNode(switchNode);
    }

    @Override
    public void visit(SwitchEntry switchEntry, CFGContext ctx) {
        var currentNode = ctx.getFollowingNode();
        var stmts = switchEntry.getStatements();

        for ( int i = stmts.size() - 1; i >= 0; --i ) {
            var stmtCtx = new CFGContext(currentNode, ctx.getLabels(), ctx.getAfterBreakNode(), ctx.getExitNode());
            stmts.get(i).accept(this, stmtCtx);
            currentNode = stmtCtx.getCurrentNode();
        }

        ctx.setCurrentNode(currentNode);
    }

    @Override
    public void visit(ReturnStmt returnStmt, CFGContext ctx) {
        var returnNode = createNode(returnStmt.toString(), CFGNodeType.STMT);
        returnNode.addSuccessor(ctx.getExitNode());
        ctx.setCurrentNode(returnNode);
    }

    private String getSwitchEntryName(SwitchEntry entry) {
        var entryName = entry.getLabels().toString();
        return entryName.length() == 2 ? "default" : entryName.substring(1, entryName.length() - 1);
    }
}
