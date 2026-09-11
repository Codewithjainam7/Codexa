package com.codexa.analysis.model;

/**
 * Data model adhering to SonarQube Generic Issue Import JSON Schema.
 */
public record SonarQubeIssueModel(
        String engineId,
        String ruleId,
        String severity,
        String type,
        PrimaryLocation primaryLocation,
        int effortMinutes
) {
    public record PrimaryLocation(
            String message,
            String filePath,
            TextRange textRange
    ) {}

    public record TextRange(
            int startLine,
            int endLine,
            int startOffset,
            int endOffset
    ) {}

    public static SonarQubeIssueModel of(String ruleId, String severity, String message, String filePath, int startLine, int endLine) {
        return new SonarQubeIssueModel(
                "codexa",
                ruleId,
                severity,
                "VULNERABILITY",
                new PrimaryLocation(message, filePath, new TextRange(startLine, endLine, 0, 0)),
                15
        );
    }
}
