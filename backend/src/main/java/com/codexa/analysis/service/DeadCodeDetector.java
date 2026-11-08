package com.codexa.analysis.service;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * CR-DEAD-001: Dead Code & Unreachable Branch Detection.
 * Identifies statements after return/throw and unused private methods.
 */
@Component
public class DeadCodeDetector implements AnalysisRule {

    @Override
    public String getRuleId() {
        return "CR-DEAD-001";
    }

    @Override
    public String getName() {
        return "Dead Code & Unreachable Branch Detection";
    }

    @Override
    public Category getCategory() {
        return Category.QUALITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.LOW;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "A04:2021 - Insecure Design";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
            Set<String> calledMethods = new HashSet<>();
            cls.findAll(MethodCallExpr.class).forEach(call -> calledMethods.add(call.getNameAsString()));

            // 1. Check for uncalled private methods
            cls.getMethods().forEach(m -> {
                if (m.isPrivate() && !calledMethods.contains(m.getNameAsString()) && !m.isAnnotationPresent("PostConstruct")) {
                    int line = m.getRange().map(r -> r.begin.line).orElse(1);
                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Unused Private Method: " + m.getNameAsString())
                            .description("Private method '" + m.getNameAsString() + "' is never invoked within the class, representing dead code and maintenance debt.")
                            .impact("Code clutter, bloated binary size, and confusing maintenance trails.")
                            .remediation("Remove unused private methods or write corresponding unit tests if intended for use.")
                            .suggestedFix("// Remove unused method '" + m.getNameAsString() + "'")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(line)
                            .endLine(line)
                            .evidence("private " + m.getType() + " " + m.getNameAsString() + "(...)")
                            .references(List.of("https://cwe.mitre.org/data/definitions/561.html"))
                            .build());
                }
            });

            // 2. Check for unreachable statements after return / throw
            cls.findAll(BlockStmt.class).forEach(block -> {
                List<Statement> stmts = block.getStatements();
                for (int i = 0; i < stmts.size() - 1; i++) {
                    Statement current = stmts.get(i);
                    if (current instanceof ReturnStmt || current instanceof ThrowStmt) {
                        Statement dead = stmts.get(i + 1);
                        int line = dead.getRange().map(r -> r.begin.line).orElse(1);
                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Unreachable Code Statement Following Return/Throw")
                                .description("Statement is placed directly after an unconditional return or throw statement and will never execute.")
                                .impact("Dead logic and developer confusion regarding intended control flow.")
                                .remediation("Delete unreachable statements or restructure conditional branching.")
                                .suggestedFix("// Delete unreachable statement at line " + line)
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(line)
                                .endLine(line)
                                .evidence(dead.toString())
                                .references(List.of("https://cwe.mitre.org/data/definitions/561.html"))
                                .build());
                    }
                }
            });
        });

        return findings;
    }
}
