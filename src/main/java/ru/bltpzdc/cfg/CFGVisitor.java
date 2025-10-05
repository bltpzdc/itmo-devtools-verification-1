package ru.bltpzdc.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
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

    public void reset() {
        nodes.clear();
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
    public void visit(AssignExpr assignExpr, CFGContext ctx) {
        var node = createNode(assignExpr.toString(), CFGNodeType.EXPR);
        node.addSuccessor(ctx.getFollowingNode());
        ctx.setCurrentNode(node);
    }

    @Override
    public void visit(UnaryExpr unaryExpr, CFGContext ctx) {
        var node = createNode(unaryExpr.toString(), CFGNodeType.EXPR);
        node.addSuccessor(ctx.getFollowingNode());
        ctx.setCurrentNode(node);
    }

    @Override
    public void visit(LabeledStmt labeledStmt, CFGContext ctx) {
        var label = labeledStmt.getLabel().toString();
        var labelNode = createNode(label, CFGNodeType.LABEL);

        var labeledCtx = new CFGContext(ctx);
        labeledCtx.getLabels().put(label, labelNode);

        labeledStmt.getStatement().accept(this, labeledCtx);
        labelNode.addSuccessor(labeledCtx.getCurrentNode());

        ctx.setCurrentNode(labelNode);
    }

    private CFGNode gotoHelper(CFGContext ctx, Optional<SimpleName> label, Optional<CFGNode> alternative) {
        var result = ctx.getFollowingNode();

        if ( label.isPresent() && ctx.getLabels().containsKey(label.get().toString()) ) {
            result = ctx.getLabels().get(label.get().toString());
        } else if ( alternative.isPresent() ) {
            result = alternative.get();
        }

        return result;
    }

    @Override
    public void visit(ContinueStmt continueStmt, CFGContext ctx) {
        ctx.setCurrentNode(gotoHelper(ctx, continueStmt.getLabel(), ctx.getCondNode()));
    }

    @Override
    public void visit(BreakStmt breakStmt, CFGContext ctx) {
        ctx.setCurrentNode(gotoHelper(ctx, breakStmt.getLabel(), ctx.getAfterBreakNode()));
    }

    @Override
    public void visit(BlockStmt blockStmt, CFGContext ctx) {
        var currentNode = ctx.getFollowingNode();
        var stmts = blockStmt.getStatements();

        for ( int i = stmts.size() - 1; i >= 0; --i ) {
            var stmtCtx = new CFGContext(currentNode, ctx.getLabels(), ctx.getAfterBreakNode(), ctx.getExitNode(), ctx.getCondNode());
            stmts.get(i).accept(this, stmtCtx);
            currentNode = stmtCtx.getCurrentNode();
        }

        ctx.setCurrentNode(currentNode);
    }

    @Override
    public void visit(IfStmt ifStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();
        var condNode = createNode(ifStmt.getCondition().toString(), CFGNodeType.COND);

        var thenCtx = new CFGContext(ctx);
        ifStmt.getThenStmt().accept(this, thenCtx);

        var elseNode = followingNode;
        if ( ifStmt.hasElseBranch() ) {
            var elseCtx = new CFGContext(ctx);
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

        var bodyCtx = new CFGContext(condNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode(), Optional.of(condNode));
        whileStmt.getBody().accept(this, bodyCtx);

        condNode.addSuccessor(bodyCtx.getCurrentNode(), "True");
        condNode.addSuccessor(followingNode, "False");

        ctx.setCurrentNode(condNode);
    }

    @Override
    public void visit(DoStmt doStmt, CFGContext ctx) {
        var followingNode = ctx.getFollowingNode();
        var condNode = createNode(doStmt.getCondition().toString(), CFGNodeType.COND);

        var bodyCtx = new CFGContext(condNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode(), Optional.of(condNode));
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
        var init = forStmt.getInitialization();
        if ( !init.isEmpty() ) {
            var currentNode = condNode;
            for ( int i = init.size() - 1; i >= 0; --i ) {
                var curCtx = new CFGContext(currentNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode(), Optional.empty());
                init.get(i).accept(this, curCtx);
                currentNode = curCtx.getCurrentNode();
            }
            initNode = currentNode;
        }

        var updateNode = condNode;
        var update = forStmt.getUpdate();
        if ( !update.isEmpty() ) {
            var currentNode = condNode;
            for ( int i = update.size() - 1; i >= 0; --i ) {
                var curCtx = new CFGContext(currentNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode(), Optional.empty());
                update.get(i).accept(this, curCtx);
                currentNode = curCtx.getCurrentNode();
            }
            updateNode = currentNode;
        }

        var bodyCtx = new CFGContext(updateNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode(), Optional.of(updateNode));
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
            var entryCtx = new CFGContext(currentNode, ctx.getLabels(), Optional.of(followingNode), ctx.getExitNode(), ctx.getCondNode());
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
            var stmtCtx = new CFGContext(currentNode, ctx.getLabels(), ctx.getAfterBreakNode(), ctx.getExitNode(), ctx.getCondNode());
            stmts.get(i).accept(this, stmtCtx);
            currentNode = stmtCtx.getCurrentNode();
        }

        ctx.setCurrentNode(currentNode);
    }

    @Override
    public void visit(ReturnStmt returnStmt, CFGContext ctx) {
        var returnNode = createNode(returnStmt.toString(), CFGNodeType.EXPR);
        returnNode.addSuccessor(ctx.getExitNode());
        ctx.setCurrentNode(returnNode);
    }

    private String getSwitchEntryName(SwitchEntry entry) {
        var entryName = entry.getLabels().toString();
        return entryName.length() == 2 ? "default" : entryName.substring(1, entryName.length() - 1);
    }

    @Override
    public void visit(VariableDeclarationExpr declExpr, CFGContext ctx) {
        var currentNode = ctx.getFollowingNode();
        var stmts = declExpr.getChildNodes();
        for ( int i = stmts.size() - 1; i >= 0; --i ) {
            var stmtCtx = new CFGContext(currentNode, ctx.getLabels(), ctx.getAfterBreakNode(), ctx.getExitNode(), ctx.getCondNode());
            stmts.get(i).accept(this, stmtCtx);
            currentNode = stmtCtx.getCurrentNode();
        }

        ctx.setCurrentNode(currentNode);
    }

    @Override
    public void visit(VariableDeclarator varDecl, CFGContext ctx) {
        var currentNode = createNode(varDecl.toString(), CFGNodeType.EXPR);
        currentNode.addSuccessor(ctx.getFollowingNode());
        ctx.setCurrentNode(currentNode);
    }
}
