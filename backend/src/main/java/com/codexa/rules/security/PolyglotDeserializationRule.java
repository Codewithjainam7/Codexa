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
 * CR-DESER-002: Insecure Deserialization in Python (Pickle/YAML) and Node.js.
 * Detects pickle.loads, yaml.load without SafeLoader, and node-serialize.unserialize.
 */
@Component
public class PolyglotDeserializationRule implements AnalysisRule {

    private static final Pattern POLYGLOT_DESER_PATTERN = Pattern.compile(
            "(?i)(?:pickle\\.loads?\\s*\\(|yaml\\.load\\s*\\([^,)]*\\)(?!.*SafeLoader)|serialize\\.unserialize\\s*\\(|jsonpickle\\.decode\\s*\\()"
    );

    @Override
    public String getRuleId() {
        return "CR-DESER-002";
    }

    @Override
    public String getName() {
        return "Polyglot Insecure Deserialization (Python & Node.js)";
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
        return "A08:2021 - Software and Data Integrity Failures";
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
                if (fileName.endsWith(".py") || fileName.endsWith(".js") || fileName.endsWith(".ts")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (POLYGLOT_DESER_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(getSeverity())
                                        .confidence(getDefaultConfidence())
                                        .title("Insecure Object Deserialization (RCE Risk)")
                                        .description("Detected deserialization of untrusted data via pickle, unsafe yaml.load, or node-serialize. Deserializing attacker-controlled payloads allows instantiation of arbitrary classes and gadget execution leading to Remote Code Execution.")
                                        .impact("Total host compromise and arbitrary remote code execution.")
                                        .remediation("Use safe data serialization formats like JSON (json.loads / JSON.parse) or enforce yaml.safe_load.")
                                        .suggestedFix("// Secure YAML loading in Python:\nimport yaml\ndata = yaml.safe_load(untrusted_stream)")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://cwe.mitre.org/data/definitions/502.html"))
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
