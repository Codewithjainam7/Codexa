package com.codexa.common.security;

/**
 * Standard HTTP security header names and baseline directives recommended by OWASP.
 */
public final class SecurityHeaderConstants {

    private SecurityHeaderConstants() {}

    public static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    public static final String X_FRAME_OPTIONS = "X-Frame-Options";
    public static final String X_XSS_PROTECTION = "X-XSS-Protection";
    public static final String REFERRER_POLICY = "Referrer-Policy";
    public static final String PERMISSIONS_POLICY = "Permissions-Policy";
    public static final String CROSS_ORIGIN_OPENER_POLICY = "Cross-Origin-Opener-Policy";
    public static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    public static final String X_DNS_PREFETCH_CONTROL = "X-DNS-Prefetch-Control";
    public static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

    public static final String VALUE_NOSNIFF = "nosniff";
    public static final String VALUE_DENY = "DENY";
    public static final String VALUE_STRICT_ORIGIN = "strict-origin-when-cross-origin";
    public static final String VALUE_SAME_ORIGIN = "same-origin";
}
