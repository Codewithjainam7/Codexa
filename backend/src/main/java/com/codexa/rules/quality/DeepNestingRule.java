package com.codexa.rules.quality;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.AstSnippetExtractor;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeepNestingRule implements AnalysisRule {

    private static final int MAX_NESTING_DEPTH = 4;
    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-QUAL-003";
    }

    @Override
    public String getName() {
        return "Deep Statement Nesting";
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
        return "Code Maintainability";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(MethodDeclaration.class).forEach(method -> {
            int maxDepth = findMaxNestingDepth(method, 0);
            if (maxDepth > MAX_NESTING_DEPTH) {
                int startLine = method.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = method.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(method, parsedFile.getLines());

                findings.add(RuleFinding.builder()
                        .ruleId(getRuleId())
                        .category(getCategory())
                        .severity(getSeverity())
                        .confidence(getDefaultConfidence())
                        .title("Deep nesting depth (" + maxDepth + " > " + MAX_NESTING_DEPTH + ") in '" + method.getNameAsString() + "'")
                        .description("Method '" + method.getNameAsString() + "' contains deeply nested control flow blocks (nesting level " + maxDepth + "). Deeply nested structures are difficult to reason about and track edge conditions.")
                        .impact("Elevated cognitive complexity and increased risk of subtle logic bypasses.")
                        .remediation("Use guard clauses / early returns (e.g. if (!condition) return;) to flatten code hierarchy.")
                        .suggestedFix("// Invert conditionals with early returns:\nif (!valid) return;\n// proceed without nesting")
                        .owaspMapping(getOwaspMapping())
                        .filePath(parsedFile.getRelativePath())
                        .startLine(startLine)
                        .endLine(endLine)
                        .evidence(evidence)
                        .references(List.of("https://refactoring.guru/flatten-conditional-blocks"))
                        .build());
            }
        });

        return findings;
    }

    private int findMaxNestingDepth(Node node, int currentDepth) {
        int max = currentDepth;
        for (Node child : node.getChildNodes()) {
            int nextDepth = isNestingConstruct(child) ? currentDepth + 1 : currentDepth;
            max = Math.max(max, findMaxNestingDepth(child, nextDepth));
        }
        return max;
    }

    private boolean isNestingConstruct(Node node) {
        return node instanceof IfStmt || node instanceof WhileStmt || node instanceof ForStmt ||
                node instanceof ForEachStmt || node instanceof DoStmt || node instanceof SwitchStmt ||
                node instanceof TryStmt;
    }
}
