package com.codexa.analysis.pipeline;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Timer utility for tracking elapsed time across pipeline execution stages with nanosecond precision.
 */
public class StageExecutionTimer {

    private final Map<String, Long> stageDurationsMs = new LinkedHashMap<>();
    private String currentStage;
    private long stageStartTimeNanos;

    public synchronized void startStage(String stageName) {
        if (currentStage != null) {
            stopStage();
        }
        this.currentStage = stageName;
        this.stageStartTimeNanos = System.nanoTime();
    }

    public synchronized void stopStage() {
        if (currentStage != null) {
            long elapsedNanos = System.nanoTime() - stageStartTimeNanos;
            long elapsedMs = Math.max(1, elapsedNanos / 1_000_000);
            stageDurationsMs.put(currentStage, elapsedMs);
            currentStage = null;
        }
    }

    public synchronized Map<String, Long> getStageDurations() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(stageDurationsMs));
    }

    public synchronized long getTotalDurationMs() {
        return stageDurationsMs.values().stream().mapToLong(Long::longValue).sum();
    }
}
