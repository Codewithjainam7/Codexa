package com.codexa.analysis.model;

public record AnalysisMetricResponse(
        double securityScore,
        double qualityScore,
        double operationsScore,
        double maintainabilityScore,
        double architecturalScore,
        int totalFiles,
        int analyzedFiles,
        int criticalCount,
        int highCount,
        int mediumCount,
        int lowCount,
        long durationMs
) {
    public AnalysisMetricResponse(
            double securityScore,
            double qualityScore,
            double operationsScore,
            double maintainabilityScore,
            int totalFiles,
            int analyzedFiles,
            int criticalCount,
            int highCount,
            int mediumCount,
            int lowCount,
            long durationMs
    ) {
        this(securityScore, qualityScore, operationsScore, maintainabilityScore, qualityScore, totalFiles, analyzedFiles, criticalCount, highCount, mediumCount, lowCount, durationMs);
    }

    public AnalysisMetricResponse(
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
        this(securityScore, qualityScore, operationsScore, qualityScore, qualityScore, totalFiles, analyzedFiles, criticalCount, highCount, mediumCount, lowCount, durationMs);
    }
}
