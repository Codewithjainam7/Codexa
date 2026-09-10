package com.codexa.analysis.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Deterministic fingerprint record for tracking and deduplicating findings across repeated scans.
 */
public record FindingFingerprint(String hash, String ruleId, String filePath, int startLine) {

    public static FindingFingerprint of(String ruleId, String filePath, int startLine) {
        String raw = (ruleId != null ? ruleId : "") + "|" +
                     (filePath != null ? filePath : "") + "|" +
                     startLine;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            String hash = HexFormat.of().formatHex(digest);
            return new FindingFingerprint(hash, ruleId, filePath, startLine);
        } catch (NoSuchAlgorithmException e) {
            return new FindingFingerprint(Integer.toHexString(raw.hashCode()), ruleId, filePath, startLine);
        }
    }
}
