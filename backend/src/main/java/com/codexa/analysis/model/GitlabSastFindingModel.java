package com.codexa.analysis.model;

/**
 * Data model adhering to GitLab SAST Report schema (v15.x).
 */
public record GitlabSastFindingModel(
        String id,
        String category,
        String name,
        String message,
        String severity,
        String confidence,
        ScannerInfo scanner,
        LocationInfo location
) {
    public record ScannerInfo(String id, String name) {}

    public record LocationInfo(String file, int startLine, int endLine) {}

    public static GitlabSastFindingModel of(String id, String name, String message, String severity, String file, int startLine, int endLine) {
        return new GitlabSastFindingModel(
                id,
                "sast",
                name,
                message,
                severity,
                "High",
                new ScannerInfo("codexa_ast", "Codexa AST Security Scanner"),
                new LocationInfo(file, startLine, endLine)
        );
    }
}
