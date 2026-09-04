package com.codexa.analysis.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnalysisJobResponse(
        UUID id,
        SourceType sourceType,
        String sourceIdentifier,
        String repositoryCommit,
        JobStatus status,
        String progressStage,
        int progressPercent,
        Double overallScore,
        ProductionVerdict verdict,
        String summary,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        Instant completedAt,
        AnalysisMetricResponse metrics,
        List<FindingResponse> topFindings
) {
}
