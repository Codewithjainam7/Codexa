package com.codexa.rules.llm;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.ParsedJavaFile;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * OWASP Top 10 for Large Language Model (LLM) Applications.
 * Audits AI pipelines for Direct Prompt Injection (LLM01) and Insecure Output Handling (LLM02).
 */
@Component
public class LlmApplicationSecurityRule implements AnalysisRule {

    private static final Pattern LLM_PROMPT_VAR_PATTERN = Pattern.compile("(?i)(prompt|systemprompt|llmprompt|chatprompt|instruction)");
    private static final Pattern LLM_CLIENT_PATTERN = Pattern.compile("(?i)(chatCompletion|generateCompletion|complete|askAi|aiClient|openAi|ollama|anthropic)");

    @Override
    public String getRuleId() {
        return "CR-LLM-001";
    }

    @Override
    public String getName() {
        return "OWASP LLM Application Security (Prompt Injection & Insecure Output)";
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
        return "LLM01:2025 - Prompt Injection";
    }

    @Override
    public String getDescription() {
        return "Detects unescaped user concatenation into LLM system prompts and untrusted execution of LLM-generated outputs.";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile javaFile = context.getParsedJavaFile();
        if (javaFile == null) return findings;

        var cuOpt = javaFile.getCompilationUnit();
        if (cuOpt.isEmpty()) return findings;
        CompilationUnit cu = cuOpt.get();

        // 1. Detect Direct Prompt Injection (String concatenation into prompt variables)
        cu.findAll(BinaryExpr.class).stream()
                .filter(b -> b.getOperator() == BinaryExpr.Operator.PLUS)
                .forEach(binary -> {
                    String str = binary.toString();
                    if (LLM_PROMPT_VAR_PATTERN.matcher(str).find() && (str.contains("input") || str.contains("user") || str.contains("request") || str.contains("query"))) {
                        int line = binary.getBegin().map(p -> p.line).orElse(1);
                        findings.add(RuleFinding.builder()
                                .ruleId("CR-LLM-001")
                                .category(Category.SECURITY)
                                .severity(Severity.HIGH)
                                .confidence(Confidence.HIGH)
                                .filePath(javaFile.getRelativePath())
                                .startLine(line)
                                .endLine(line)
                                .title("Direct LLM Prompt Injection via Dynamic String Concatenation")
                                .description("Unsanitized user variable is concatenated directly into LLM prompt text, enabling prompt override / jailbreak attacks.")
                                .impact("Malicious user prompts can bypass AI guardrails, exfiltrate system instructions, or hijack downstream tool execution.")
                                .remediation("Use structured chat role messages (`user`, `system`) and sanitize inputs through an explicit guardrail / delimiter enclosure.")
                                .suggestedFix("List.of(Map.of(\"role\", \"system\", \"content\", systemPrompt), Map.of(\"role\", \"user\", \"content\", sanitizedInput))")
                                .evidence(binary.toString().substring(0, Math.min(100, binary.toString().length())))
                                .owaspMapping("LLM01:2025 - Prompt Injection")
                                .references(List.of("https://owasp.org/www-project-top-10-for-large-language-model-applications/"))
                                .build());
                    }
                });

        // 2. Detect Insecure Output Handling (passing AI output to shell or database)
        cu.findAll(MethodCallExpr.class).stream()
                .filter(m -> m.getNameAsString().equals("exec") || m.getNameAsString().equals("execute") || m.getNameAsString().equals("executeQuery"))
                .forEach(call -> {
                    String callStr = call.toString();
                    if (callStr.contains("response") || callStr.contains("llm") || callStr.contains("aiOutput") || callStr.contains("content")) {
                        int line = call.getBegin().map(p -> p.line).orElse(1);
                        findings.add(RuleFinding.builder()
                                .ruleId("CR-LLM-002")
                                .category(Category.SECURITY)
                                .severity(Severity.CRITICAL)
                                .confidence(Confidence.HIGH)
                                .filePath(javaFile.getRelativePath())
                                .startLine(line)
                                .endLine(line)
                                .title("Insecure LLM Output Handling (Direct Execution of AI Content)")
                                .description("Output generated by LLM is passed directly into a privileged execution sink (" + call.getNameAsString() + ") without schema validation.")
                                .impact("Prompt injection against the AI model directly translates into Remote Code Execution (RCE) or SQL Injection on the host.")
                                .remediation("Treat all LLM responses as untrusted user input. Validate against strict schemas before passing to execution APIs.")
                                .suggestedFix("// Validate and parse LLM output against an immutable DTO schema before execution")
                                .evidence(call.toString())
                                .owaspMapping("LLM02:2025 - Insecure Output Handling")
                                .references(List.of("https://owasp.org/www-project-top-10-for-large-language-model-applications/"))
                                .build());
                    }
                });

        return findings;
    }
}
