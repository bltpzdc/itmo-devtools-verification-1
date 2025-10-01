package ru.bltpzdc.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.Pair;

public class CFGBuilder {
    private final CFGVisitor visitor;

    public CFGBuilder() {
        this.visitor = new CFGVisitor();
    }

    public Pair<Map<String, CFGNode>, CFGNode> build(MethodDeclaration method) {
        visitor.reset();

        var entryNode = visitor.createNode(method.getDeclarationAsString(), CFGNodeType.ENTRY);
        var exitNode = visitor.createNode("EXIT", CFGNodeType.EXIT);

        if ( method.getBody().isPresent() ) {
            var body = method.getBody().get();
            var ctx = new CFGContext(exitNode, new HashMap<>(), Optional.empty(), exitNode, Optional.empty());

            body.accept(visitor, ctx);

            entryNode.addSuccessor(ctx.getCurrentNode());
        } else {
            entryNode.addSuccessor(exitNode);
        }

        return new Pair<>(visitor.getNodes(), entryNode);
    }


}
