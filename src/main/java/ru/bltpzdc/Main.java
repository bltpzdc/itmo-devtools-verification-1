package com.example;

import java.io.IOException;
import java.nio.file.Paths;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

public class Main {
    public static void main(String[] args) {
        if ( args.length < 1 ) {
            System.err.println("Filename should be provided via args");
            return;
        }

        CompilationUnit unit;
        try {
            unit = StaticJavaParser.parse(Paths.get(args[0]));
        } catch (IOException e) {
            System.err.println("Unable to read from " + args[0]);
            return;
        }

        var methods = unit.findAll(MethodDeclaration.class).stream()
            .map(method -> method.getName())
            .toList();

        System.out.println(methods);
    }
}