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
 * CR-RACE-001: Time-of-Check to Time-of-Use (TOCTOU) Race Condition.
 * Detects non-atomic file presence or state checks immediately preceding operations without locks or atomic primitives.
 */
@Component
public class ToctouRaceConditionRule implements AnalysisRule {

    private static final Pattern FS_CHECK_PATTERN = Pattern.compile(
            "(?i)(?:\\.exists\\(\\)|fs\\.existsSync|os\\.path\\.exists|access\\()"
    );

    private static final Pattern FS_OPERATE_PATTERN = Pattern.compile(
            "(?i)(?:\\.createNewFile\\(\\)|fs\\.writeFileSync|open\\(|\\.delete\\(\\)|unlink\\()"
    );

    @Override
    public String getRuleId() {
        return "CR-RACE-001";
    }

    @Override
    public String getName() {
        return "Time-of-Check to Time-of-Use (TOCTOU) Race Condition";
    }

    @Override
    public Category getCategory() {
        return Category.SECURITY;
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
                if (fileName.endsWith(".java") || fileName.endsWith(".js") || fileName.endsWith(".ts") || fileName.endsWith(".py")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size() - 4; i++) {
                            String checkLine = lines.get(i);
                            if (FS_CHECK_PATTERN.matcher(checkLine).find()) {
                                for (int j = i + 1; j <= i + 4; j++) {
                                    String operateLine = lines.get(j);
                                    if (FS_OPERATE_PATTERN.matcher(operateLine).find()) {
                                        findings.add(RuleFinding.builder()
                                                .ruleId(getRuleId())
                                                .category(getCategory())
                                                .severity(getSeverity())
                                                .confidence(getDefaultConfidence())
                                                .title("Potential TOCTOU Race Condition on File System")
                                                .description("Detected file existence check followed closely by an independent file operation. Between the check and the operation, an attacker or concurrent process can alter, replace, or symlink the file (Time-of-Check to Time-of-Use).")
                                                .impact("Insecure file overwrite, symlink arbitrary write, or concurrent state corruption.")
                                                .remediation("Use atomic file operations (e.g. StandardOpenOption.CREATE_NEW in Java, or 'wx' open flags in Node.js) and handle exceptions rather than pre-checking.")
                                                .suggestedFix("// Secure atomic creation:\nFiles.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);")
                                                .owaspMapping(getOwaspMapping())
                                                .filePath(relPath)
                                                .startLine(i + 1)
                                                .endLine(j + 1)
                                                .evidence(checkLine.trim() + " -> " + operateLine.trim())
                                                .references(List.of("https://cwe.mitre.org/data/definitions/367.html"))
                                                .build());
                                        break;
                                    }
                                }
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
