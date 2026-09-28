package com.codexa.analysis.model;

/**
 * Represents a known CVE security advisory for a software supply chain dependency.
 */
public record CveAdvisory(
        String cveId,
        String componentName,
        String ecosystem,
        String vulnerableVersionMatcher, // regex or exact match
        String fixedVersion,
        double cvssScore,
        Severity severity,
        String summary,
        String cwe,
        String referenceUrl
) {}
