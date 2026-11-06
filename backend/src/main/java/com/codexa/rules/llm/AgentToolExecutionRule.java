package com.codexa.rules.llm;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * CR-LLM-003: Insecure Autonomous Agent Tool Execution & Sink Hijack.
 * Flags AI agent systems that wire unverified LLM output into system shells, raw SQL queries, or file write sinks.
 */
@Component
public class AgentToolExecutionRule implements AnalysisRule {

    private static final Pattern UNCONSTRAINED_AGENT_TOOL_PATTERN = Pattern.compile(
            "(?i)(?:BashProcess|PythonREPL|TerminalTool|ShellTool|SystemTool|execute_shell|run_bash|eval_code)\\s*\\("
    );

    private static final Pattern LLM_RAW_EXEC_PATTERN = Pattern.compile(
            "(?i)(?:os\\.system|subprocess\\.(?:call|run|Popen)|child_process\\.exec|Runtime\\.getRuntime\\(\\)\\.exec)\\s*\\([^)]*(?:response|completion|message|output|tool_call|function_args)"
    );

    @Override
    public String getRuleId() {
        return "CR-LLM-003";
    }

    @Override
    public String getName() {
        return "Insecure AI Agent Tool Execution (OWASP LLM 2025)";
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
        return "LLM07:2025 - Insecure Plugin & Tool Design";
    }

    @Override
    public boolean isRepositoryWide() {
        return true;
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        Path stagingDir = context.getStagingDirectory();

        if (stagingDir == null || !Files.exists(stagingDir)) {
            return findings;
        }

        try (Stream<Path> stream = Files.walk(stagingDir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                if (fileName.endsWith(".py") || fileName.endsWith(".ts") || fileName.endsWith(".js") || fileName.endsWith(".java")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (UNCONSTRAINED_AGENT_TOOL_PATTERN.matcher(line).find() || LLM_RAW_EXEC_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(getSeverity())
                                        .confidence(getDefaultConfidence())
                                        .title("Autonomous AI Agent Unconstrained Tool Execution Sink")
                                        .description("Detected an autonomous AI agent wiring model-generated completions or tool call outputs directly into command execution or interactive REPL tools. A prompt injection or poisoned context can trigger arbitrary Remote Code Execution.")
                                        .impact("Total host takeover via indirect prompt injection, arbitrary command execution, and sensitive data exfiltration.")
                                        .remediation("Implement Human-in-the-Loop (HITL) approval gates before executing state-changing tools, sandbox execution in containerized microVMs (e.g. Firecracker, gVisor), and strictly whitelist permitted commands.")
                                        .suggestedFix("// Secure HITL Agent tool execution pattern:\nif (!requireUserConfirmation(toolName, parameters)) {\n  throw new SecurityException('Tool execution rejected by user');\n}\nexecuteSandboxed(toolName, parameters);")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of(
                                                "https://genai.owasp.org/llmrisk/llm07-insecure-plugin-design/",
                                                "https://cwe.mitre.org/data/definitions/74.html"
                                        ))
                                        .build());
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        } catch (IOException ignored) {
        }

        return findings;
    }
}
