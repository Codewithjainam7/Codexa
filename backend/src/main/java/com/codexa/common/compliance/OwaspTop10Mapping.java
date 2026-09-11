package com.codexa.common.compliance;

/**
 * OWASP Top 10:2021 categories for classifying static rule findings.
 */
public enum OwaspTop10Mapping {
    A01_BROKEN_ACCESS_CONTROL("A01:2021", "Broken Access Control"),
    A02_CRYPTOGRAPHIC_FAILURES("A02:2021", "Cryptographic Failures"),
    A03_INJECTION("A03:2021", "Injection"),
    A04_INSECURE_DESIGN("A04:2021", "Insecure Design"),
    A05_SECURITY_MISCONFIGURATION("A05:2021", "Security Misconfiguration"),
    A06_VULNERABLE_COMPONENTS("A06:2021", "Vulnerable and Outdated Components"),
    A07_IDENTIFICATION_AUTH_FAILURES("A07:2021", "Identification and Authentication Failures"),
    A08_SOFTWARE_DATA_INTEGRITY("A08:2021", "Software and Data Integrity Failures"),
    A09_LOGGING_MONITORING_FAILURES("A09:2021", "Security Logging and Monitoring Failures"),
    A10_SSRF("A10:2021", "Server-Side Request Forgery (SSRF)");

    private final String code;
    private final String title;

    OwaspTop10Mapping(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }
}
