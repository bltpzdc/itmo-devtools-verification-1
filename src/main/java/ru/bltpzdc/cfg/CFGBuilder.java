package ru.bltpzdc.cfg;

import java.util.HashMap;
import java.util.Optional;

import com.github.javaparser.ast.body.MethodDeclaration;

public class CFGBuilder {
    CFGVisitor visitor;

    public CFGNode buildCFG(MethodDeclaration method) {
        visitor = new CFGVisitor();
        System.out.println(method.getName());

        var entryNode = visitor.createNode("ENTRY", NodeType.ENTRY);
        var exitNode = visitor.createNode("EXIT", NodeType.EXIT);

        if ( method.getBody().isPresent() ) {
            var body = method.getBody().get();
            var ctx = new CFGContext(exitNode, new HashMap<>(), Optional.empty());

            body.accept(visitor, ctx);

            entryNode.addSuccessor(ctx.getCurrentNode());
        } else {
            entryNode.addSuccessor(exitNode);
        }

        return entryNode;
    }


}
