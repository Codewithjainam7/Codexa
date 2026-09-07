package com.codexa.rules.security;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.AstSnippetExtractor;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PathTraversalRule implements AnalysisRule {

    private static final Set<String> DANGEROUS_FILE_TYPES = Set.of(
            "File", "FileInputStream", "FileOutputStream", "FileReader", "FileWriter", "RandomAccessFile"
    );

    private static final Set<String> DANGEROUS_PATH_METHODS = Set.of(
            "get", "of", "resolve", "resolveSibling"
    );

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-SEC-003";
    }

    @Override
    public String getName() {
        return "Path Traversal & Arbitrary File Access";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.HIGH;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "A01:2021-Broken Access Control";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        // 1. Check ObjectCreationExpr: new File(..., dynamic), new FileInputStream(dynamic), etc.
        cu.findAll(ObjectCreationExpr.class).forEach(creation -> {
            String typeName = creation.getTypeAsString();
            if (DANGEROUS_FILE_TYPES.contains(typeName)) {
                for (Expression arg : creation.getArguments()) {
                    if (containsDynamicConcatenation(arg)) {
                        int startLine = creation.getRange().map(r -> r.begin.line).orElse(1);
                        int endLine = creation.getRange().map(r -> r.end.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(creation, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Path Traversal Vulnerability in '" + typeName + "' Construction")
                                .description("Constructing a '" + typeName + "' with dynamically concatenated paths or untrusted parameters without canonicalization allows attackers to traverse outside the designated directory via '../' sequences (CWE-22).")
                                .impact("Unauthorized arbitrary file read, overwrite, or information disclosure across system storage.")
                                .remediation("Normalize input paths using Path.normalize() and verify that path.toRealPath().startsWith(baseDirectory) before performing file I/O.")
                                .suggestedFix("""
                                        Path destination = baseDir.resolve(fileName).normalize();
                                        if (!destination.startsWith(baseDir)) {
                                            throw new SecurityException("Directory traversal attempt detected: " + fileName);
                                        }
                                        """)
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .references(List.of(
                                        "https://owasp.org/www-community/attacks/Path_Traversal",
                                        "https://cwe.mitre.org/data/definitions/22.html"
                                ))
                                .build());
                        break;
                    }
                }
            }
        });

        // 2. Check Paths.get(dynamic), Path.of(dynamic)
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String scope = call.getScope().map(Object::toString).orElse("");
            String methodName = call.getNameAsString();
            if (("Paths".equals(scope) || "Path".equals(scope)) && DANGEROUS_PATH_METHODS.contains(methodName)) {
                for (Expression arg : call.getArguments()) {
                    if (containsDynamicConcatenation(arg)) {
                        int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                        int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Unvalidated Path Resolution in '" + scope + "." + methodName + "()'")
                                .description("Resolving file paths with dynamically concatenated strings without validation allows directory traversal.")
                                .impact("Arbitrary path traversal and unauthorized filesystem operations.")
                                .remediation("Enforce canonical path validation with normalize() and startsWith(baseDir).")
                                .suggestedFix("""
                                        Path safePath = baseDir.resolve(userInput).normalize();
                                        if (!safePath.startsWith(baseDir)) {
                                            throw new SecurityException("Access denied: Invalid directory path");
                                        }
                                        """)
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .references(List.of("https://cwe.mitre.org/data/definitions/22.html"))
                                .build());
                        break;
                    }
                }
            }
        });

        return findings;
    }

    private boolean containsDynamicConcatenation(Expression expr) {
        if (expr instanceof BinaryExpr binaryExpr) {
            return binaryExpr.getOperator() == BinaryExpr.Operator.PLUS;
        }
        return false;
    }
}
