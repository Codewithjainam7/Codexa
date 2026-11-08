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
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.TryStmt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * CR-MEMORY-001: Unclosed Native / Stream Resource Leak.
 * Detects AutoCloseable objects instantiated outside of try-with-resources blocks.
 */
@Component
public class UnclosedResourceRule implements AnalysisRule {

    private static final Set<String> CLOSEABLE_TYPES = Set.of(
            "FileInputStream", "FileOutputStream", "FileReader", "FileWriter",
            "BufferedReader", "BufferedWriter", "Connection", "Statement",
            "PreparedStatement", "ResultSet", "ZipFile", "JarFile"
    );

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-MEMORY-001";
    }

    @Override
    public String getName() {
        return "Unclosed Stream & Native Resource Leak";
    }

    @Override
    public Category getCategory() {
        return Category.QUALITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.MEDIUM;
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

        cu.findAll(VariableDeclarationExpr.class).forEach(varDecl -> {
            varDecl.getVariables().forEach(variable -> {
                String typeName = variable.getTypeAsString();
                if (CLOSEABLE_TYPES.contains(typeName)) {
                    // Check if enclosed in try-with-resources statement
                    boolean isTryWithResources = varDecl.getParentNode()
                            .filter(p -> p instanceof TryStmt tryStmt && tryStmt.getResources().contains(varDecl))
                            .isPresent();

                    if (!isTryWithResources) {
                        int startLine = varDecl.getRange().map(r -> r.begin.line).orElse(1);
                        int endLine = varDecl.getRange().map(r -> r.end.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(varDecl, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("Unclosed Resource Leak: " + typeName)
                                .description("Instantiated closeable resource '" + typeName + "' without try-with-resources. If an exception occurs during processing, the native file descriptor or connection will leak, causing operating system descriptor exhaustion.")
                                .impact("File descriptor exhaustion, database connection pool starvation, and server degradation.")
                                .remediation("Manage the closeable resource inside a Java try-with-resources block or ensure deterministic cleanup in a finally block.")
                                .suggestedFix("// Secure try-with-resources:\ntry (" + typeName + " res = ...) {\n    // use resource\n}")
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .references(List.of("https://cwe.mitre.org/data/definitions/775.html"))
                                .build());
                    }
                }
            });
        });

        return findings;
    }
}
