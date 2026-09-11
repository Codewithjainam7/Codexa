package com.codexa.analysis.pipeline;

import java.time.Instant;
import java.util.Map;

/**
 * Summary record capturing total execution metrics, findings count, and stage latency.
 */
public record PipelineExecutionSummary(
        String jobId,
        String status,
        int totalFilesScanned,
        int totalLinesScanned,
        int totalFindingsDiscovered,
        long durationMs,
        Map<String, Long> stageLatenciesMs,
        Instant completedAt
) {}
