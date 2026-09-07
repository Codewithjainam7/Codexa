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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CsrfProtectionRule implements AnalysisRule {

    private static final Set<String> MUTATING_METHODS = Set.of(
            "delete", "deleteAll", "deleteById", "save", "saveAll", "saveAndFlush", "insert", "update", "drop"
    );

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-SEC-006";
    }

    @Override
    public String getName() {
        return "Cross-Site Request Forgery (CSRF) & State Mutation in Safe Method";
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

        // 1. Detect state modification inside @GetMapping
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            boolean hasGetMapping = method.getAnnotations().stream()
                    .anyMatch(a -> "GetMapping".equals(a.getNameAsString()));

            if (hasGetMapping) {
                for (MethodCallExpr call : method.findAll(MethodCallExpr.class)) {
                    if (MUTATING_METHODS.contains(call.getNameAsString())) {
                        int startLine = method.getRange().map(r -> r.begin.line).orElse(1);
                        int endLine = method.getRange().map(r -> r.end.line).orElse(startLine);
                        String evidence = snippetExtractor.extractNodeSnippet(method, parsedFile.getLines());

                        findings.add(RuleFinding.builder()
                                .ruleId(getRuleId())
                                .category(getCategory())
                                .severity(getSeverity())
                                .confidence(getDefaultConfidence())
                                .title("State Mutation in Safe HTTP GET Endpoint '" + method.getNameAsString() + "'")
                                .description("Method '" + method.getNameAsString() + "' is mapped with @GetMapping but performs state-mutating operations ('" + call.getNameAsString() + "'). HTTP GET methods must remain idempotent and safe according to RFC 7231; performing mutations in GET exposes the application to CSRF via simple image tags or hyperlinks (CWE-352).")
                                .impact("Cross-Site Request Forgery (CSRF) enabling attackers to trick authenticated users into executing state mutations.")
                                .remediation("Change mapping to @PostMapping, @PutMapping, or @DeleteMapping and enforce CSRF token verification.")
                                .suggestedFix("""
                                        @PostMapping("/path") // Use POST/DELETE for mutating operations
                                        public ResponseEntity<?> handleMutation(...) { ... }
                                        """)
                                .owaspMapping(getOwaspMapping())
                                .filePath(parsedFile.getRelativePath())
                                .startLine(startLine)
                                .endLine(endLine)
                                .evidence(evidence)
                                .references(List.of(
                                        "https://owasp.org/www-community/attacks/csrf",
                                        "https://cwe.mitre.org/data/definitions/352.html"
                                ))
                                .build());
                        break;
                    }
                }
            }
        });

        // 2. Check csrf.disable() in Spring Security config
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            String methodName = call.getNameAsString();
            if ("disable".equals(methodName)) {
                String scope = call.getScope().map(Object::toString).orElse("");
                if (scope.contains("csrf")) {
                    int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                    int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(Severity.MEDIUM)
                            .confidence(Confidence.HIGH)
                            .title("CSRF Protection Disabled in Security Configuration")
                            .description("Explicitly calling 'csrf().disable()' removes protection against Cross-Site Request Forgery (CWE-352). For session-based web applications, CSRF protection is required.")
                            .impact("Vulnerability to CSRF attacks if session cookies are used for authentication.")
                            .remediation("Keep CSRF protection enabled for browser-facing sessions, or document stateless token exemption.")
                            .suggestedFix("// Ensure CSRF is only disabled for stateless pure-REST APIs using Bearer tokens")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of("https://cwe.mitre.org/data/definitions/352.html"))
                            .build());
                }
            }
        });

        return findings;
    }
}
