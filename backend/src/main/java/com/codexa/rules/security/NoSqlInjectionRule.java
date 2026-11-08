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
 * CR-NOSQL-001: NoSQL Operator Injection & Unsafe $where Evaluation.
 * Detects unescaped client input passed into MongoDB/NoSQL queries and Javascript $where execution blocks.
 */
@Component
public class NoSqlInjectionRule implements AnalysisRule {

    private static final Pattern NOSQL_WHERE_PATTERN = Pattern.compile(
            "(?i)\\$where\\s*:\\s*(?:`[^`]*\\$\\{|f[\"'][^\"']*\\{|[\"'][^\"']*[\"']\\s*\\+)"
    );

    private static final Pattern RAW_BODY_NOSQL_PATTERN = Pattern.compile(
            "(?i)(?:\\.findOne|\\.find|\\.countDocuments|\\.deleteMany|\\.updateMany)\\s*\\(\\s*req\\.body"
    );

    @Override
    public String getRuleId() {
        return "CR-NOSQL-001";
    }

    @Override
    public String getName() {
        return "NoSQL Operator Injection & $where Code Execution";
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
                if (fileName.endsWith(".js") || fileName.endsWith(".ts") || fileName.endsWith(".py") || fileName.endsWith(".java")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (NOSQL_WHERE_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(Severity.CRITICAL)
                                        .confidence(getDefaultConfidence())
                                        .title("MongoDB $where JavaScript Code Execution Injection")
                                        .description("Detected dynamic concatenation inside MongoDB '$where' clause. This allows attackers to inject and execute arbitrary server-side JavaScript inside the database daemon.")
                                        .impact("Server-side code execution in the database layer and unauthorized data compromise.")
                                        .remediation("Eliminate the use of $where clauses. Rely strictly on standard MongoDB query operators ($eq, $in, $gte).")
                                        .suggestedFix("// Secure remediation:\ndb.collection.find({ status: sanitizedStatus });")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://cwe.mitre.org/data/definitions/943.html"))
                                        .build());
                            }

                            if (RAW_BODY_NOSQL_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(Severity.HIGH)
                                        .confidence(getDefaultConfidence())
                                        .title("NoSQL Operator Injection via Raw Request Body")
                                        .description("Detected raw req.body passed directly as query filter to NoSQL find/findOne call. An attacker can supply JSON containing NoSQL query operators (e.g. {\"password\": {\"$ne\": null}}) to bypass authentication checks.")
                                        .impact("Authentication bypass and unauthorized records exfiltration.")
                                        .remediation("Sanitize input using express-mongo-sanitize or explicitly map validated string primitives to query keys.")
                                        .suggestedFix("// Secure primitive extraction:\ndb.users.findOne({ username: String(req.body.username) });")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://cwe.mitre.org/data/definitions/943.html"))
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
