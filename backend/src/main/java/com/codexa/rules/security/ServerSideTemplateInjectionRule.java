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
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * CR-SSTI-001: Server-Side Template Injection (SSTI).
 * Detects dynamic concatenation or evaluation of untrusted user input within template rendering engines
 * such as FreeMarker, Thymeleaf, Velocity, Pebble, or Handlebars.
 */
@Component
public class ServerSideTemplateInjectionRule implements AnalysisRule {

    private static final Set<String> TEMPLATE_EVAL_METHODS = Set.of(
            "process", "evaluate", "createTemplate", "getTemplate", "render", "merge"
    );

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-SSTI-001";
    }

    @Override
    public String getName() {
        return "Server-Side Template Injection (SSTI)";
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
        return "A03:2021 - Injection";
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
            if (TEMPLATE_EVAL_METHODS.contains(methodName) && call.getArguments().isNonEmpty()) {
                boolean hasDynamicInput = call.getArguments().stream().anyMatch(arg -> {
                    if (arg instanceof BinaryExpr binaryExpr) {
                        return binaryExpr.getOperator() == BinaryExpr.Operator.PLUS;
                    }
                    String text = arg.toString().toLowerCase();
                    return text.contains("req") || text.contains("param") || text.contains("input");
                });

                if (hasDynamicInput) {
                    int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                    int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                    String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Server-Side Template Injection via Dynamic Concatenation")
                            .description("Detected unvalidated input or string concatenation passed to template engine invocation '" + methodName + "'. An attacker can inject arbitrary template directives (e.g. ${T(java.lang.Runtime).getRuntime().exec(...)}), leading to Remote Code Execution.")
                            .impact("Remote Code Execution (RCE), complete host takeover, and file exfiltration.")
                            .remediation("Never concatenate untrusted input into template strings. Pass dynamic data as context variables to static template files, and enforce a sandboxed template environment.")
                            .suggestedFix("// Secure remediation:\nTemplate template = cfg.getTemplate(\"static_view.ftl\");\nMap<String, Object> data = new HashMap<>();\ndata.put(\"userName\", sanitizedUser);\ntemplate.process(data, writer);")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(evidence)
                            .references(List.of(
                                    "https://cwe.mitre.org/data/definitions/1336.html",
                                    "https://portswigger.net/web-security/server-side-template-injection"
                            ))
                            .build());
                }
            }
        });

        return findings;
    }
}
