package com.codexa.analysis.model;

import java.util.List;
import java.util.Map;

public record ProjectDiagnostics(
        CodeComposition composition,
        WhiteBoxDiagnostics whiteBox,
        BlackBoxDiagnostics blackBox,
        List<ComplianceCheckItem> complianceChecklist
) {
    public record CodeComposition(
            int totalLines,
            int codeLines,
            int commentLines,
            int blankLines,
            Map<String, Integer> languageLoc,
            Map<String, Integer> languageFiles
    ) {}

    public record WhiteBoxDiagnostics(
            double avgComplexity,
            int peakComplexity,
            String peakComplexityFile,
            int totalClasses,
            int totalMethods,
            int totalInterfaces,
            int maxNestingDepth,
            List<FileComplexityMetric> topComplexFiles,
            List<CodeSmellMetric> smellsDistribution
    ) {}

    public record BlackBoxDiagnostics(
            List<ApiEndpointItem> exposedEndpoints,
            int totalEndpoints,
            int unauthenticatedEndpoints,
            PerimeterStatus perimeterStatus
    ) {}

    public record ApiEndpointItem(
            String httpMethod,
            String path,
            String controllerClass,
            String methodName,
            boolean requiresAuth,
            String attackSurfaceRisk
    ) {}

    public record PerimeterStatus(
            String corsStatus,
            String rateLimitingStatus,
            String securityHeadersStatus,
            String secretsExposureStatus
    ) {}

    public record ComplianceCheckItem(
            String title,
            String category,
            String status,
            String detail
    ) {}

    public record FileComplexityMetric(
            String filePath,
            int loc,
            int methodCount,
            int maxComplexity,
            double avgComplexity,
            int findingCount
    ) {}

    public record CodeSmellMetric(
            String ruleId,
            String title,
            String category,
            int count
    ) {}
}
