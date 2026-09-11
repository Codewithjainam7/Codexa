package com.codexa.common.security;

import java.util.regex.Pattern;

/**
 * Utility for verifying private and loopback IP ranges to detect SSRF vulnerabilities.
 */
public final class IpAddressValidator {

    private IpAddressValidator() {}

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.){3}(25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)$"
    );

    public static boolean isLoopback(String host) {
        if (host == null) return false;
        String h = host.trim().toLowerCase();
        return h.equals("localhost") || h.equals("127.0.0.1") || h.equals("::1") || h.startsWith("127.");
    }

    public static boolean isPrivateIp(String ip) {
        if (ip == null || !IPV4_PATTERN.matcher(ip).matches()) return false;
        String[] parts = ip.split("\\.");
        int first = Integer.parseInt(parts[0]);
        int second = Integer.parseInt(parts[1]);

        if (first == 10) return true; // 10.0.0.0/8
        if (first == 172 && second >= 16 && second <= 31) return true; // 172.16.0.0/12
        if (first == 192 && second == 168) return true; // 192.168.0.0/16
        if (first == 127) return true; // loopback
        if (first == 169 && second == 254) return true; // link-local (cloud metadata)
        return false;
    }
}
