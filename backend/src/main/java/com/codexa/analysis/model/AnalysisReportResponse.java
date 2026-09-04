package com.codexa.analysis.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AnalysisReportResponse(
        UUID jobId,
        String appName,
        String scanTarget,
        SourceType sourceType,
        Double overallScore,
        ProductionVerdict verdict,
        String summary,
        Instant scanDate,
        AnalysisMetricResponse metrics,
        List<FindingResponse> findings,
        String disclaimer
) {
    public static final String STANDARD_DISCLAIMER =
            "DISCLAIMER: Codexa is an automated static code review and advisory tool, not a guarantee of security or compliance certification. " +
            "A clean scan does not prove the total absence of vulnerabilities, and all suggested remediations require human engineering review and testing.";
}
