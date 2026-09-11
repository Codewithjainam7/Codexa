package com.codexa.common.compliance;

/**
 * PCI-DSS v4.0 standard requirement mapping for AST static analysis rules.
 */
public enum PciDssRequirement {
    REQ_3_4("3.4", "Render PAN unreadable anywhere it is stored using strong cryptography"),
    REQ_6_2_4("6.2.4", "Mitigate common software vulnerabilities including injection, XSS, and broken access control"),
    REQ_6_3_1("6.3.1", "Identify and manage security vulnerabilities using reputable sources"),
    REQ_8_3_1("8.3.1", "Strong authentication and password hashing algorithms"),
    REQ_10_2_1("10.2.1", "Audit logs generated for all access to system components and data");

    private final String requirementId;
    private final String description;

    PciDssRequirement(String requirementId, String description) {
        this.requirementId = requirementId;
        this.description = description;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public String getDescription() {
        return description;
    }
}
