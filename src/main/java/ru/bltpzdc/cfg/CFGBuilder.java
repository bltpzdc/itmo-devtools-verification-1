package ru.bltpzdc.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.Pair;

public class CFGBuilder {
    private CFGVisitor visitor;

    // TODO: make build()
    public Pair<Map<String, CFGNode>, CFGNode> buildCFG(MethodDeclaration method) {
        visitor = new CFGVisitor();

        var entryNode = visitor.createNode(method.getDeclarationAsString(), CFGNodeType.ENTRY);
        var exitNode = visitor.createNode("EXIT", CFGNodeType.EXIT);

        if ( method.getBody().isPresent() ) {
            var body = method.getBody().get();
            var ctx = new CFGContext(exitNode, new HashMap<>(), Optional.empty(), exitNode);

            body.accept(visitor, ctx);

            entryNode.addSuccessor(ctx.getCurrentNode());
        } else {
            entryNode.addSuccessor(exitNode);
        }

        return new Pair<>(visitor.getNodes(), entryNode);
    }


}
