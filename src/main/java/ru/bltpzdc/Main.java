package ru.bltpzdc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import ru.bltpzdc.cfg.CFGBuilder;
import ru.bltpzdc.dot.DOTGenerator;

public class Main {
    public static void main(String[] args) {
        if ( args.length < 1 ) {
            System.err.println("usage: java -jar <jar> <filename> [<output-dir>]");
            return;
        }

        String filename = args[0];
        String outputDir = args.length > 1
                       ? args[1] + Paths.get(filename).getFileName().toString().replace(".java", "")
                       : "";

        CompilationUnit unit;
        try {
            unit = StaticJavaParser.parse(Paths.get(filename));
        } catch (IOException e) {
            System.err.println("Unable to read from " + filename);
            return;
        }

        CFGBuilder cfgBuilder = new CFGBuilder();
        for ( var method : unit.findAll(MethodDeclaration.class) ) {
            var result = cfgBuilder.build(method);
            var dot = DOTGenerator.convert(result.a);

            if ( !outputDir.isEmpty() ) {
                String outputFile = outputDir + String.format(".%s.dot", method.getName());
                try {
                    Files.write(Paths.get(outputFile), dot.getBytes());
                } catch (IOException e) {
                    System.err.println("Unable to write in " + outputFile);
                }
            } else {
                System.out.println(method.getName() + ":");
                System.out.println(dot);
            }
        }
    }
}