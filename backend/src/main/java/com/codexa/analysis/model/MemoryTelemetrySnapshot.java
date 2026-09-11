package com.codexa.analysis.model;

/**
 * Snapshot of JVM heap and non-heap memory utilization captured during pipeline execution.
 */
public record MemoryTelemetrySnapshot(
        long totalMemoryBytes,
        long freeMemoryBytes,
        long maxMemoryBytes,
        long usedMemoryBytes,
        double usedPercentage
) {
    public static MemoryTelemetrySnapshot capture() {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long max = rt.maxMemory();
        long used = total - free;
        double pct = total > 0 ? ((double) used / total) * 100.0 : 0.0;
        return new MemoryTelemetrySnapshot(total, free, max, used, Math.round(pct * 100.0) / 100.0);
    }
}
