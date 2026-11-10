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
 * CR-HTTP-001: HTTP Request Smuggling & Header Desync.
 * Flags proxy forwarding configs and custom server dispatchers vulnerable to CL.TE / TE.CL desynchronization.
 */
@Component
public class HttpRequestSmugglingRule implements AnalysisRule {

    private static final Pattern HOP_BY_HOP_PATTERN = Pattern.compile(
            "(?i)setHeader\\s*\\(\\s*[\"'](?:Transfer-Encoding|Content-Length)[\"']|header\\s*\\(\\s*[\"']Transfer-Encoding[\"']"
    );

    private static final Pattern PROXY_PASS_UNSAFE = Pattern.compile(
            "(?i)proxy_set_header\\s+Connection\\s+[\"']?\\$http_connection|proxy_http_version\\s+1\\.0"
    );

    @Override
    public String getRuleId() {
        return "CR-HTTP-001";
    }

    @Override
    public String getName() {
        return "HTTP Request Smuggling & Hop-by-Hop Header Desync";
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
                if (fileName.endsWith(".java") || fileName.endsWith(".js") || fileName.endsWith(".ts") ||
                    fileName.endsWith(".conf") || fileName.contains("nginx")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (HOP_BY_HOP_PATTERN.matcher(line).find() || PROXY_PASS_UNSAFE.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(getSeverity())
                                        .confidence(getDefaultConfidence())
                                        .title("HTTP Request Smuggling / Header Desync Risk")
                                        .description("Detected manual manipulation of Transfer-Encoding/Content-Length or insecure proxy header forwarding. Discrepancies between reverse-proxy and backend framing lead to request smuggling, session hijacking, and cache poisoning.")
                                        .impact("Bypass of front-end security controls, unauthorized request execution, and credential exfiltration.")
                                        .remediation("Rely on framework HTTP framing; do not manually set Transfer-Encoding or forward raw connection headers to backends.")
                                        .suggestedFix("// Secure remediation: Enforce HTTP/2 end-to-end and reject ambiguous Transfer-Encoding headers.")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://cwe.mitre.org/data/definitions/444.html", "https://portswigger.net/web-security/request-smuggling"))
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
