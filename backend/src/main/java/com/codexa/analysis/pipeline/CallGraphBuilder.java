package com.codexa.analysis.pipeline;

import com.codexa.analysis.model.CallGraph;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Constructs an Interprocedural Call Graph across parsed Java compilation units.
 */
@Service
public class CallGraphBuilder {

    public CallGraph build(List<ParsedJavaFile> files) {
        CallGraph graph = new CallGraph();

        if (files == null || files.isEmpty()) {
            return graph;
        }

        for (ParsedJavaFile file : files) {
            if (file.getCompilationUnit().isEmpty()) continue;
            CompilationUnit cu = file.getCompilationUnit().get();

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
                String className = cls.getNameAsString();

                cls.findAll(MethodDeclaration.class).forEach(method -> {
                    String methodName = method.getNameAsString();
                    int methodLine = method.getRange().map(r -> r.begin.line).orElse(1);
                    CallGraph.Node callerNode = new CallGraph.Node(className, methodName, methodLine, file.getRelativePath());

                    method.findAll(MethodCallExpr.class).forEach(call -> {
                        String calleeMethod = call.getNameAsString();
                        String scope = call.getScope().map(s -> s.toString()).orElse(className);
                        int callLine = call.getRange().map(r -> r.begin.line).orElse(methodLine);

                        CallGraph.Node calleeNode = new CallGraph.Node(scope, calleeMethod, callLine, file.getRelativePath());
                        graph.addEdge(callerNode, calleeNode, callLine);
                    });
                });
            });
        }

        return graph;
    }
}
