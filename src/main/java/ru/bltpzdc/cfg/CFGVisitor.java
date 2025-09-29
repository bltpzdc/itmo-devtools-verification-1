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
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class CFGVisitor extends VoidVisitorAdapter<CFGContext> {
    private final Map<String, CFGNode> nodes;
    private static long nodeIDGenerator = 0;

    public CFGVisitor() {
        this.nodes = new HashMap<>();
    }

    public CFGNode createNode(String label, NodeType type) {
        var id = "node_" + nodeIDGenerator++;
        var node = new CFGNode(id, label, type);
        nodes.put(id, node);

        return node;
    }

    @Override
    public void visit(LabeledStmt labeledStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();

        var label = labeledStmt.getLabel().toString();
        var labelNode = createNode(label, NodeType.LABEL);

        var labeledCtx = new CFGContext(followingNode, ctx.getLabels(), ctx.getAfterBreakNode());
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
            var stmtCtx = new CFGContext(currentNode, ctx.getLabels(), ctx.getAfterBreakNode());
            stmts.get(i).accept(this, stmtCtx);
            currentNode = stmtCtx.getCurrentNode();
        }

        ctx.setCurrentNode(currentNode);
    }

    @Override
    public void visit(ExpressionStmt exprStmt, CFGContext ctx) {
        var expr = exprStmt.toString();
        var stmtNode = createNode(expr, NodeType.STMT);
        stmtNode.addSuccessor(ctx.getFollowingNode());
        ctx.setCurrentNode(stmtNode);
    }

    @Override
    public void visit(IfStmt ifStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();
        var condNode = createNode(ifStmt.getCondition().toString(), NodeType.COND);

        var thenCtx = new CFGContext(followingNode, ctx.getLabels(), ctx.getAfterBreakNode());
        ifStmt.getThenStmt().accept(this, thenCtx);

        var elseNode = followingNode;
        if ( ifStmt.hasElseBranch() ) {
            var elseCtx = new CFGContext(followingNode, ctx.getLabels(), ctx.getAfterBreakNode());
            ifStmt.getElseStmt().get().accept(this, elseCtx);
            elseNode = elseCtx.getCurrentNode();
        }

        condNode.addSuccessor(thenCtx.getCurrentNode());
        condNode.addSuccessor(elseNode);

        ctx.setCurrentNode(condNode);
    }
    
    @Override
    public void visit(WhileStmt whileStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();
        var condNode = createNode(whileStmt.getCondition().toString(), NodeType.COND);

        var bodyCtx = new CFGContext(condNode, ctx.getLabels(), Optional.of(followingNode));
        whileStmt.getBody().accept(this, bodyCtx);

        condNode.addSuccessor(bodyCtx.getCurrentNode());
        condNode.addSuccessor(followingNode);

        ctx.setCurrentNode(condNode);
    }

    @Override
    public void visit(DoStmt doStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();
        var condNode = createNode(doStmt.getCondition().toString(), NodeType.COND);

        var bodyCtx = new CFGContext(condNode, ctx.getLabels(), Optional.of(followingNode));
        doStmt.getBody().accept(this, bodyCtx);

        condNode.addSuccessor(followingNode);
        condNode.addSuccessor(bodyCtx.getCurrentNode());

        ctx.setCurrentNode(bodyCtx.getCurrentNode());
    }

    @Override
    public void visit(ForStmt forStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();

        var cond = forStmt.getCompare().map(expr -> expr.toString()).orElse("true");
        var condNode = createNode(cond, NodeType.COND);
        
        var initNode = condNode;
        var init = forStmt.getInitialization().toString();
        if ( !init.equals("[]") ) {
            initNode = createNode(init, NodeType.STMT);
            initNode.addSuccessor(condNode);
        }

        var update = forStmt.getUpdate().toString();
        var updateNode = condNode;
        if ( !update.equals("[]") ) {
            updateNode = createNode(update, NodeType.STMT);
            updateNode.addSuccessor(condNode);
        }

        var bodyCtx = new CFGContext(updateNode, ctx.getLabels(), Optional.of(followingNode));
        forStmt.getBody().accept(this, bodyCtx);

        condNode.addSuccessor(bodyCtx.getCurrentNode());
        condNode.addSuccessor(followingNode);

        ctx.setCurrentNode(initNode);
    }

    @Override
    public void visit(SwitchStmt switchStmt, CFGContext ctx) {

    }
}
