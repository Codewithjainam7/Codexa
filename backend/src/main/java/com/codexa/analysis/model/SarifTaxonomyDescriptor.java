package com.codexa.analysis.model;

/**
 * Descriptor for SARIF external taxonomies (CWE, OWASP).
 */
public record SarifTaxonomyDescriptor(
        String name,
        String version,
        String informationUri,
        String organization
) {
    public static SarifTaxonomyDescriptor cwe() {
        return new SarifTaxonomyDescriptor("CWE", "4.13", "https://cwe.mitre.org/", "MITRE");
    }

    public static SarifTaxonomyDescriptor owasp() {
        return new SarifTaxonomyDescriptor("OWASP Top 10", "2021", "https://owasp.org/Top10/", "OWASP");
    }
}
