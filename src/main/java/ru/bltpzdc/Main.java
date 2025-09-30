package ru.bltpzdc;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.utils.Pair;

import ru.bltpzdc.cfg.CFGBuilder;
import ru.bltpzdc.cfg.CFGNode;
import ru.bltpzdc.dot.DOTGenerator;

public class Main {
    public static void main(String[] args) {
        // if ( args.length < 1 ) {
        //     System.err.println("Filename should be provided via args");
        //     return;
        // }

        CompilationUnit unit;
        try {
            unit = StaticJavaParser.parse(Paths.get("/home/sevastian/ITMO/ipkn/grade1/verification/lab1/cfg/src/main/resources/Test.java"));
        } catch (IOException e) {
            System.err.println("Unable to read from " + args[0]);
            return;
        }

        List<Pair<SimpleName, CFGNode>> methods = new ArrayList<>();
        CFGBuilder cfgBuilder = new CFGBuilder();
        for ( var method : unit.findAll(MethodDeclaration.class) ) {
            var result = cfgBuilder.buildCFG(method);
            methods.add(new Pair<>(method.getName(), result.b));
            System.out.println(methods);
            System.out.println(result.a);
            System.out.println(DOTGenerator.convert(method.getName().toString(), result.a).get());
        }
    }
}