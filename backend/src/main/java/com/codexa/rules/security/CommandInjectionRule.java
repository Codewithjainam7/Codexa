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
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommandInjectionRule implements AnalysisRule {

    private final AstSnippetExtractor snippetExtractor = new AstSnippetExtractor();

    @Override
    public String getRuleId() {
        return "CR-CMD-001";
    }

    @Override
    public String getName() {
        return "OS Command Injection";
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

        // 1. Check Runtime.getRuntime().exec()
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            if ("exec".equals(call.getNameAsString()) && call.getArguments().isNonEmpty()) {
                int startLine = call.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = call.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(call, parsedFile.getLines());

                findings.add(createFinding(parsedFile.getRelativePath(), startLine, endLine, evidence, "Runtime.getRuntime().exec()"));
            }
        });

        // 2. Check new ProcessBuilder()
        cu.findAll(ObjectCreationExpr.class).forEach(creation -> {
            if ("ProcessBuilder".equals(creation.getTypeAsString())) {
                int startLine = creation.getRange().map(r -> r.begin.line).orElse(1);
                int endLine = creation.getRange().map(r -> r.end.line).orElse(startLine);
                String evidence = snippetExtractor.extractNodeSnippet(creation, parsedFile.getLines());

                findings.add(createFinding(parsedFile.getRelativePath(), startLine, endLine, evidence, "ProcessBuilder"));
            }
        });

        return findings;
    }

    private RuleFinding createFinding(String filePath, int startLine, int endLine, String evidence, String mechanism) {
        return RuleFinding.builder()
                .ruleId(getRuleId())
                .category(getCategory())
                .severity(getSeverity())
                .confidence(getDefaultConfidence())
                .title("Potential OS command execution via " + mechanism)
                .description("Detected invocation of OS command execution via " + mechanism + ". If any input passed into this process is controlled by an untrusted user, attackers can execute arbitrary operating system commands.")
                .impact("Complete host system takeover, remote code execution (RCE), and lateral movement within the infrastructure.")
                .remediation("Avoid executing operating system commands from application logic. Use native Java library APIs instead. If external process execution is unavoidable, use strict whitelisting of allowed commands and arguments without shell interpolation.")
                .suggestedFix("// Avoid OS execution; use standard Java NIO or dedicated libraries instead:\n// e.g., Files.copy(source, destination);")
                .owaspMapping(getOwaspMapping())
                .filePath(filePath)
                .startLine(startLine)
                .endLine(endLine)
                .evidence(evidence)
                .references(List.of("https://owasp.org/Top10/2025/A05_2025-Injection/", "https://cheatsheetseries.owasp.org/cheatsheets/OS_Command_Injection_Defense_Cheat_Sheet.html"))
                .build();
    }
}
