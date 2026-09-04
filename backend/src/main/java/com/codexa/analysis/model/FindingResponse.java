package com.codexa.analysis.model;

import java.util.List;
import java.util.UUID;

public record FindingResponse(
        UUID id,
        String ruleId,
        Category category,
        Severity severity,
        Confidence confidence,
        String title,
        String description,
        String impact,
        String remediation,
        String owaspMapping,
        String filePath,
        int startLine,
        int endLine,
        String evidenceMasked,
        String suggestedFix,
        double priorityScore,
        boolean requiresManualReview,
        List<String> references
) {
}
