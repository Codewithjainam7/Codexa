package com.codexa.analysis.service;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.*;
import org.springframework.stereotype.Service;

/**
 * Cognitive Complexity Calculation Engine (SonarQube Specification).
 * Computes human understandability penalties based on nesting depth and flow breaks.
 */
@Service
public class CognitiveComplexityCalculator {

    public int calculate(MethodDeclaration method) {
        if (method == null || method.getBody().isEmpty()) {
            return 0;
        }

        return evaluateBlock(method.getBody().get(), 0);
    }

    private int evaluateBlock(Node node, int nestingLevel) {
        int complexity = 0;

        for (Node child : node.getChildNodes()) {
            if (child instanceof IfStmt ifStmt) {
                complexity += 1 + nestingLevel;
                complexity += evaluateBlock(ifStmt.getThenStmt(), nestingLevel + 1);
                if (ifStmt.getElseStmt().isPresent()) {
                    Statement elseStmt = ifStmt.getElseStmt().get();
                    if (!(elseStmt instanceof IfStmt)) {
                        complexity += 1; // 'else' clause adds 1
                    }
                    complexity += evaluateBlock(elseStmt, nestingLevel);
                }
            } else if (child instanceof ForStmt || child instanceof ForEachStmt || child instanceof WhileStmt || child instanceof DoStmt) {
                complexity += 1 + nestingLevel;
                complexity += evaluateBlock(child, nestingLevel + 1);
            } else if (child instanceof CatchClause) {
                complexity += 1 + nestingLevel;
                complexity += evaluateBlock(child, nestingLevel + 1);
            } else if (child instanceof SwitchStmt) {
                complexity += 1 + nestingLevel;
                complexity += evaluateBlock(child, nestingLevel + 1);
            } else if (child instanceof ConditionalExpr) {
                complexity += 1 + nestingLevel;
                complexity += evaluateBlock(child, nestingLevel + 1);
            } else {
                complexity += evaluateBlock(child, nestingLevel);
            }
        }

        return complexity;
    }
}
