package com.codexa.common.security;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Constant-time comparison utility for authentication tokens and API keys to prevent timing attacks.
 */
public final class TokenValidator {

    private TokenValidator() {}

    public static boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = actual.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
