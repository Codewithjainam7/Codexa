package com.codexa.analysis.model;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Snapshot of total garbage collection cycles and cumulative pause latency.
 */
public record GcTelemetrySnapshot(
        long totalGcCount,
        long totalGcTimeMs
) {
    public static GcTelemetrySnapshot capture() {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long count = 0;
        long time = 0;
        for (GarbageCollectorMXBean gc : gcBeans) {
            long c = gc.getCollectionCount();
            long t = gc.getCollectionTime();
            if (c > 0) count += c;
            if (t > 0) time += t;
        }
        return new GcTelemetrySnapshot(count, time);
    }
}
