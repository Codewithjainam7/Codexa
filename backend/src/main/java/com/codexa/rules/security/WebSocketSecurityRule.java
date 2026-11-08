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
 * CR-WEBSOCKET-001: Missing WebSocket Origin Verification & Cross-Site WebSocket Hijacking (CSWSH).
 * Detects WebSocket server instances accepting connections without verifying the Origin header.
 */
@Component
public class WebSocketSecurityRule implements AnalysisRule {

    private static final Pattern WS_SERVER_PATTERN = Pattern.compile(
            "(?i)(?:new\\s+WebSocketServer|new\\s+Server\\s*\\(\\s*\\{\\s*server|@EnableWebSocket|WebSocketConfigurer|addEndpoint|ServerEndpoint)"
    );

    private static final Pattern PERMISSIVE_WS_ORIGIN_PATTERN = Pattern.compile(
            "(?i)(?:setAllowedOrigins\\s*\\(\\s*[\"']\\*[\"']\\)|origin\\s*:\\s*[\"']\\*[\"']|verifyClient\\s*:\\s*\\(\\s*\\)\\s*=>\\s*true|checkOrigin\\s*=\\s*nil)"
    );

    @Override
    public String getRuleId() {
        return "CR-WEBSOCKET-001";
    }

    @Override
    public String getName() {
        return "Insecure WebSocket Origin Validation (CSWSH)";
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
        return "A01:2021 - Broken Access Control";
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
                if (fileName.endsWith(".js") || fileName.endsWith(".ts") || fileName.endsWith(".java") || fileName.endsWith(".go")) {
                    try {
                        String content = Files.readString(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        if (WS_SERVER_PATTERN.matcher(content).find() && PERMISSIVE_WS_ORIGIN_PATTERN.matcher(content).find()) {
                            findings.add(RuleFinding.builder()
                                    .ruleId(getRuleId())
                                    .category(getCategory())
                                    .severity(getSeverity())
                                    .confidence(getDefaultConfidence())
                                    .title("Wildcard WebSocket Origin Verification (CSWSH)")
                                    .description("WebSocket endpoint accepts connections from any origin ('*') without verifying the HTTP Origin header during the connection handshake. Malicious third-party websites can hijack the victim's session cookies and initiate unauthorized bidirectional socket communication.")
                                    .impact("Cross-Site WebSocket Hijacking (CSWSH), unauthorized real-time command execution, and data exfiltration.")
                                    .remediation("Validate the HTTP Origin header against a strict allowlist of trusted domain names during the WebSocket handshake.")
                                    .suggestedFix("// Secure WebSocket handshake origin verification:\nconst wss = new WebSocketServer({\n  server,\n  verifyClient: ({ origin }, cb) => {\n    const allowed = ['https://myapp.com', 'https://admin.myapp.com'];\n    cb(allowed.includes(origin));\n  }\n});")
                                    .owaspMapping(getOwaspMapping())
                                    .filePath(relPath)
                                    .startLine(1)
                                    .endLine(1)
                                    .evidence("WebSocket configured with wildcard origin ('*')")
                                    .references(List.of(
                                            "https://cwe.mitre.org/data/definitions/1385.html",
                                            "https://portswigger.net/web-security/websockets/cross-site-websocket-hijacking"
                                    ))
                                    .build());
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
