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
 * CR-REDIS-001: Redis Command Injection & Unsanitized Lua Eval.
 * Detects unsanitized user variables concatenated into Redis eval/evalsha calls, raw commands, or keys.
 */
@Component
public class RedisSecurityRule implements AnalysisRule {

    private static final Pattern REDIS_EVAL_PATTERN = Pattern.compile(
            "(?i)(?:redis|client|jedis|connection|cluster)\\.(?:eval|evalsha)\\s*\\(\\s*(?:`[^`]*\\$\\{|f[\"'][^\"']*\\{|[\"'][^\"']*[\"']\\s*\\+)"
    );

    private static final Pattern REDIS_RAW_CMD_PATTERN = Pattern.compile(
            "(?i)(?:redis|jedis|client)\\.sendCommand\\s*\\([^)]*\\+"
    );

    @Override
    public String getRuleId() {
        return "CR-REDIS-001";
    }

    @Override
    public String getName() {
        return "Redis Command Injection & Unsanitized Lua Eval";
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
                if (fileName.endsWith(".java") || fileName.endsWith(".js") || fileName.endsWith(".ts") || fileName.endsWith(".py")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (REDIS_EVAL_PATTERN.matcher(line).find() || REDIS_RAW_CMD_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(Severity.HIGH)
                                        .confidence(getDefaultConfidence())
                                        .title("Redis Lua Script / Command Injection")
                                        .description("Detected dynamic string concatenation or template interpolation inside Redis eval() or sendCommand(). An attacker can escape the Lua sandbox, rewrite redis keys, poison cache entries, or execute unauthorized commands (e.g. FLUSHALL, CONFIG SET).")
                                        .impact("Cache poisoning, unauthorized data alteration, and Redis denial of service.")
                                        .remediation("Pass dynamic parameters as explicit ARGV/KEYS arguments to Redis eval rather than concatenating values into the Lua script string.")
                                        .suggestedFix("// Secure Redis Lua script execution:\nString script = \"return redis.call('get', KEYS[1])\";\njedis.eval(script, Collections.singletonList(userKey), Collections.emptyList());")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of(
                                                "https://cwe.mitre.org/data/definitions/94.html",
                                                "https://redis.io/docs/interact/programmability/eval-intro/"
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
