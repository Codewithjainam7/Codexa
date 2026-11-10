package com.codexa.rules.security;

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
 * CR-PROTO-001: Server-Side Prototype Pollution via Object Traversal.
 * Detects recursive merges, bracket notation assignments on __proto__, and prototype pollution sinks.
 */
@Component
public class ServerPrototypePollutionRule implements AnalysisRule {

    private static final Pattern PROTO_POLLUTION_PATTERN = Pattern.compile(
            "(?i)\\[['\"](?:__proto__|prototype|constructor)['\"]\\]|\\[key\\]\\s*=\\s*val|target\\[k\\]\\s*=\\s*source\\[k\\]"
    );

    private static final Pattern UNSAFE_MERGE_PATTERN = Pattern.compile(
            "(?i)function\\s+merge\\s*\\([^)]*\\)\\s*\\{[\\s\\S]*?for\\s*\\([\\s\\S]*?\\["
    );

    @Override
    public String getRuleId() {
        return "CR-PROTO-001";
    }

    @Override
    public String getName() {
        return "Server-Side Prototype Pollution via Recursive Object Traversal";
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
                if (fileName.endsWith(".js") || fileName.endsWith(".ts")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (PROTO_POLLUTION_PATTERN.matcher(line).find() && !line.contains("hasOwnProperty") && !line.contains("hasOwn")) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(getSeverity())
                                        .confidence(getDefaultConfidence())
                                        .title("Server-Side Prototype Pollution via Dynamic Bracket Assignment")
                                        .description("Detected unvalidated object property assignment using dynamic bracket keys without filtering '__proto__' or 'constructor'. Malicious payloads can inject properties into Object.prototype, altering application logic or leading to Remote Code Execution.")
                                        .impact("Denial of Service, bypass of authorization checks, and arbitrary command execution.")
                                        .remediation("Drop '__proto__' and 'constructor' keys before recursive assignment, or utilize Object.create(null) for dictionary maps.")
                                        .suggestedFix("// Secure object key filtering:\nif (key === '__proto__' || key === 'constructor') return;\nObject.defineProperty(target, key, { value, enumerable: true });")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://cwe.mitre.org/data/definitions/1321.html"))
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
