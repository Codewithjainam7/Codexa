package com.codexa.analysis.model;

public record AnalysisMetricResponse(
        double securityScore,
        double qualityScore,
        double operationsScore,
        int totalFiles,
        int analyzedFiles,
        int criticalCount,
        int highCount,
        int mediumCount,
        int lowCount,
        long durationMs
) {
}
