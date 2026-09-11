package com.codexa.common.security;

import java.util.regex.Pattern;

/**
 * Utility for masking secrets, bearer tokens, API keys, and credentials before logging or serialization.
 */
public final class SensitiveDataSanitizer {

    private SensitiveDataSanitizer() {}

    private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9\\-_\\.=]+");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(api[_-]?key|secret|password|token)\\s*[=:]\\s*['\"]?([^'\"\\s]{4,})['\"]?");

    public static String maskBearerToken(String input) {
        if (input == null) return null;
        return BEARER_PATTERN.matcher(input).replaceAll("Bearer [REDACTED]");
    }

    public static String maskCredentials(String input) {
        if (input == null) return null;
        return API_KEY_PATTERN.matcher(input).replaceAll("$1=***REDACTED***");
    }

    public static String maskAll(String input) {
        if (input == null) return null;
        return maskCredentials(maskBearerToken(input));
    }
}
