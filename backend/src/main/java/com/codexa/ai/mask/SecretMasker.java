package com.codexa.ai.mask;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SecretMasker {

    private static final Pattern AWS_KEY_PATTERN = Pattern.compile("(?<![A-Z0-9])((?:AKIA|ASIA)[A-Z0-9]{16})(?![A-Z0-9])");
    private static final Pattern GITHUB_TOKEN_PATTERN = Pattern.compile("(?<![a-zA-Z0-9_])((?:ghp|gho|ghu|ghs|ghr)_[A-Za-z0-9_]{36,40})(?![a-zA-Z0-9_])");
    private static final Pattern OPENAI_KEY_PATTERN = Pattern.compile("(?<![a-zA-Z0-9_-])(sk-(?:proj-)?[a-zA-Z0-9_-]{20,})(?![a-zA-Z0-9_-])");
    private static final Pattern JWT_TOKEN_PATTERN = Pattern.compile("(?<![a-zA-Z0-9_-])(eyJ[a-zA-Z0-9_-]{10,}\\.eyJ[a-zA-Z0-9_-]{10,}\\.[a-zA-Z0-9_-]{10,})(?![a-zA-Z0-9_-])");
    private static final Pattern GENERIC_TOKEN_PATTERN = Pattern.compile(
            "(?i)(?:api[_-]?key|secret[_-]?key|jwt[_-]?secret|access[_-]?token|password|db_pass|client_secret|private[_-]?key)\\s*[:=]\\s*[\"']?([^\"'\\s]{6,})[\"']?"
    );
    private static final Pattern PRIVATE_KEY_BLOCK_PATTERN = Pattern.compile(
            "-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----[\\s\\S]*?-----END (?:RSA |EC |DSA )?PRIVATE KEY-----"
    );

    private static final SecretMasker DEFAULT_INSTANCE = new SecretMasker();

    public static String maskSecrets(String input) {
        return DEFAULT_INSTANCE.mask(input);
    }

    public String mask(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String result = input;

        // 1. Redact Private Key blocks entirely
        result = PRIVATE_KEY_BLOCK_PATTERN.matcher(result).replaceAll("-----BEGIN PRIVATE KEY-----\n[REDACTED_PRIVATE_KEY_BYTES]\n-----END PRIVATE KEY-----");

        // 2. Redact AWS Keys (preserve 13 chars prefix, mask remaining)
        Matcher awsMatcher = AWS_KEY_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (awsMatcher.find()) {
            String key = awsMatcher.group(1);
            String masked = key.substring(0, Math.min(13, key.length())) + "*".repeat(Math.max(0, key.length() - 13));
            awsMatcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        awsMatcher.appendTail(sb);
        result = sb.toString();

        // 3. Redact GitHub Tokens
        Matcher ghMatcher = GITHUB_TOKEN_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (ghMatcher.find()) {
            String token = ghMatcher.group(1);
            String masked = token.substring(0, Math.min(14, token.length())) + "*".repeat(Math.max(0, token.length() - 14));
            ghMatcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        ghMatcher.appendTail(sb);
        result = sb.toString();

        // 4. Redact OpenAI / Vendor Keys
        Matcher aiMatcher = OPENAI_KEY_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (aiMatcher.find()) {
            String key = aiMatcher.group(1);
            String masked = key.substring(0, Math.min(20, key.length())) + "*".repeat(Math.max(0, key.length() - 20));
            aiMatcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        aiMatcher.appendTail(sb);
        result = sb.toString();

        // 5. Redact JWT Tokens
        Matcher jwtMatcher = JWT_TOKEN_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (jwtMatcher.find()) {
            String jwt = jwtMatcher.group(1);
            String masked = jwt.substring(0, 16) + "*".repeat(Math.max(0, jwt.length() - 16));
            jwtMatcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        jwtMatcher.appendTail(sb);
        result = sb.toString();

        // 6. Redact generic assignments
        Matcher genMatcher = GENERIC_TOKEN_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (genMatcher.find()) {
            String secret = genMatcher.group(1);
            if (!isPlaceholder(secret)) {
                String masked = "*".repeat(secret.length());
                genMatcher.appendReplacement(sb, Matcher.quoteReplacement(genMatcher.group(0).replace(secret, masked)));
            }
        }
        genMatcher.appendTail(sb);

        return sb.toString();
    }

    private boolean isPlaceholder(String val) {
        String lower = val.toLowerCase();
        return lower.contains("placeholder") || lower.contains("example") || lower.contains("your_") ||
                lower.contains("change_me") || lower.contains("dummy") || lower.equals("password");
    }
}
