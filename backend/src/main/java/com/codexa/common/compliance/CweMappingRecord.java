package com.codexa.common.compliance;

/**
 * Formal CWE taxonomy entry for AST vulnerability mapping.
 */
public record CweMappingRecord(
        int cweId,
        String name,
        String taxonomyUri,
        OwaspTop10Mapping owaspCategory
) {
    public String getFormattedId() {
        return "CWE-" + cweId;
    }

    public static CweMappingRecord of(int cweId, String name, OwaspTop10Mapping owasp) {
        return new CweMappingRecord(cweId, name, "https://cwe.mitre.org/data/definitions/" + cweId + ".html", owasp);
    }
}
