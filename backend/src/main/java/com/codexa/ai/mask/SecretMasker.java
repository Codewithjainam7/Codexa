package com.codexa.ai.mask;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SecretMasker {

    private static final Pattern AWS_KEY_PATTERN = Pattern.compile("(?<![A-Z0-9])[A-Z0-9]{20}(?![A-Z0-9])");
    private static final Pattern AWS_SECRET_PATTERN = Pattern.compile("(?<![A-Za-z0-9/+=])[A-Za-z0-9/+=]{40}(?![A-Za-z0-9/+=])");
    private static final Pattern GENERIC_TOKEN_PATTERN = Pattern.compile(
            "(?i)(?:api[_-]?key|secret[_-]?key|jwt[_-]?secret|access[_-]?token|password)\\s*[:=]\\s*[\"']?([^\"'\\s]{8,})[\"']?"
    );
    private static final Pattern PRIVATE_KEY_BLOCK_PATTERN = Pattern.compile(
            "-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----[\\s\\S]*?-----END (?:RSA |EC |DSA )?PRIVATE KEY-----"
    );

    public String mask(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String result = input;

        // 1. Redact Private Key blocks entirely
        result = PRIVATE_KEY_BLOCK_PATTERN.matcher(result).replaceAll("-----BEGIN PRIVATE KEY-----\n[REDACTED_PRIVATE_KEY_BYTES]\n-----END PRIVATE KEY-----");

        // 2. Redact AWS Keys
        Matcher awsMatcher = AWS_KEY_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (awsMatcher.find()) {
            String key = awsMatcher.group();
            if (key.startsWith("AKIA") || key.startsWith("ASIA")) {
                awsMatcher.appendReplacement(sb, key.substring(0, 4) + "****************");
            }
        }
        awsMatcher.appendTail(sb);
        result = sb.toString();

        // 3. Redact generic assignments
        Matcher genMatcher = GENERIC_TOKEN_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (genMatcher.find()) {
            String secret = genMatcher.group(1);
            if (!isPlaceholder(secret)) {
                String masked = secret.length() > 3 ? secret.substring(0, 2) + "*".repeat(secret.length() - 2) : "****";
                genMatcher.appendReplacement(sb, genMatcher.group(0).replace(secret, masked));
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
