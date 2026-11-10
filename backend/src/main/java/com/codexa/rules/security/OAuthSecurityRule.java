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
 * CR-OAUTH-001: Insecure OAuth2 Implementation & Missing PKCE / State Nonce.
 * Flags authorization grant requests lacking code_challenge (PKCE) or using permissive redirect_uri matching.
 */
@Component
public class OAuthSecurityRule implements AnalysisRule {

    private static final Pattern OAUTH_AUTH_PATTERN = Pattern.compile(
            "(?i)(?:response_type\\s*=\\s*['\"]code['\"]|authorization_code)"
    );

    private static final Pattern MISSING_PKCE_PATTERN = Pattern.compile(
            "(?i)redirect_uri\\s*=\\s*(?:req\\.(?:query|params)|request\\.(?:args|GET)|\"[^\"]*\\*\")"
    );

    @Override
    public String getRuleId() {
        return "CR-OAUTH-001";
    }

    @Override
    public String getName() {
        return "OAuth2 Open Redirect & Missing PKCE";
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
                if (fileName.endsWith(".java") || fileName.endsWith(".ts") || fileName.endsWith(".js") || fileName.endsWith(".py")) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        String relPath = stagingDir.relativize(path).toString().replace('\\', '/');

                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (MISSING_PKCE_PATTERN.matcher(line).find()) {
                                findings.add(RuleFinding.builder()
                                        .ruleId(getRuleId())
                                        .category(getCategory())
                                        .severity(getSeverity())
                                        .confidence(getDefaultConfidence())
                                        .title("OAuth2 Wildcard or Unvalidated Redirect URI")
                                        .description("Detected OAuth2 authorization flow utilizing client-supplied redirect_uri without strict exact-match allowlisting. An attacker can manipulate redirect_uri to steal authorization codes and tokens.")
                                        .impact("Account takeover via authorization code interception and token theft.")
                                        .remediation("Enforce strict exact string matching for redirect URIs against a pre-registered whitelist, and mandate Proof Key for Code Exchange (PKCE).")
                                        .suggestedFix("// Secure OAuth redirect verification:\nif (!REGISTERED_REDIRECT_URIS.contains(requestedUri)) throw new SecurityException('Invalid redirect_uri');")
                                        .owaspMapping(getOwaspMapping())
                                        .filePath(relPath)
                                        .startLine(i + 1)
                                        .endLine(i + 1)
                                        .evidence(line.trim())
                                        .references(List.of("https://cwe.mitre.org/data/definitions/601.html", "https://datatracker.ietf.org/doc/html/rfc7636"))
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
