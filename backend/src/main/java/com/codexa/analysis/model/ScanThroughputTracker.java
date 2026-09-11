package com.codexa.analysis.model;

/**
 * Utility for computing scan speed in lines per second and files per second.
 */
public final class ScanThroughputTracker {

    private ScanThroughputTracker() {}

    public static double calculateLinesPerSecond(long totalLines, long durationMs) {
        if (durationMs <= 0 || totalLines <= 0) return 0.0;
        double seconds = durationMs / 1000.0;
        return Math.round((totalLines / seconds) * 100.0) / 100.0;
    }

    public static double calculateFilesPerSecond(long totalFiles, long durationMs) {
        if (durationMs <= 0 || totalFiles <= 0) return 0.0;
        double seconds = durationMs / 1000.0;
        return Math.round((totalFiles / seconds) * 100.0) / 100.0;
    }
}
