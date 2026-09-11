package com.codexa.analysis.model;

/**
 * Vulnerability import format for DefectDojo DevSecOps Orchestrator.
 */
public record DefectDojoFindingModel(
        String title,
        String description,
        String severity,
        String mitigation,
        String impact,
        int cwe,
        String filePath,
        int lineNumber,
        boolean active,
        boolean verified
) {
    public static DefectDojoFindingModel of(String title, String desc, String sev, String fix, int cwe, String path, int line) {
        return new DefectDojoFindingModel(title, desc, sev, fix, "Potential security exploitation", cwe, path, line, true, true);
    }
}
