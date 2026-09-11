package com.codexa.analysis.model;

import java.io.File;

/**
 * Disk usage telemetry for the temporary analysis ingestion staging directory.
 */
public record DiskUsageSnapshot(
        String path,
        long totalSpaceBytes,
        long freeSpaceBytes,
        long usableSpaceBytes
) {
    public static DiskUsageSnapshot forDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return new DiskUsageSnapshot(dir != null ? dir.getAbsolutePath() : "unknown", 0, 0, 0);
        }
        return new DiskUsageSnapshot(
                dir.getAbsolutePath(),
                dir.getTotalSpace(),
                dir.getFreeSpace(),
                dir.getUsableSpace()
        );
    }

    public double getUsablePercentage() {
        if (totalSpaceBytes <= 0) return 0.0;
        return Math.round(((double) usableSpaceBytes / totalSpaceBytes) * 10000.0) / 100.0;
    }
}
