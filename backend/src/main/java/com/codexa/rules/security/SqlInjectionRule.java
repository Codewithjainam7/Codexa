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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SqlInjectionRule implements AnalysisRule {

    private static final Set<String> SQL_METHODS = Set.of(
            "executeQuery", "executeUpdate", "execute",
            "createNativeQuery", "createQuery", "rawQuery",
            "queryForObject", "queryForList", "query"
    );

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-SQL-001";
    }

    @Override
    public String getName() {
        return "SQL Injection via Unsanitized Concatenation";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.CRITICAL;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.HIGH;
    }

    @Override
    public String getOwaspMapping() {
        return "A05:2025 - Injection";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getCompilationUnit().isEmpty()) {
            return findings;
        }

        CompilationUnit cu = parsedFile.getCompilationUnit().get();

        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String methodName = call.getNameAsString();
            if (SQL_METHODS.contains(methodName) && call.getArguments().isNonEmpty()) {
                Expression firstArg = call.getArgument(0);
                if (hasStringConcatenation(firstArg)) {
                    int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                    int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("SQL query built via dynamic string concatenation")
                            .description("Detected dynamic SQL query construction using string concatenation (+ operator) passed to '" + methodName + "'. This allows attackers to manipulate SQL syntax and execute unauthorized database commands.")
                            .impact("Total compromise of database confidentiality, unauthorized data exfiltration, modification, or deletion.")
                            .remediation("Use parameterized queries (e.g. PreparedStatement with '?' placeholders or Spring Data JPA named parameters ':param'). Never concatenate untrusted variables into SQL strings.")
                            .suggestedFix("// Secure remediation:\nString sql = \"SELECT * FROM users WHERE username = ?\";\nPreparedStatement ps = conn.prepareStatement(sql);\nps.setString(1, username);")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of("https://owasp.org/Top10/2025/A05_2025-Injection/", "https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html"))
                            .build());
                }
            }
        });

        return findings;
    }

    private boolean hasStringConcatenation(Expression expr) {
        if (expr instanceof BinaryExpr binaryExpr) {
            return binaryExpr.getOperator() == BinaryExpr.Operator.PLUS;
        }
        return false;
    }
}
