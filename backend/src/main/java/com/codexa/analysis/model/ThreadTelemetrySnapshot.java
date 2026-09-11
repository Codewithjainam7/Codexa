package com.codexa.analysis.model;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Snapshot of active thread count and peak thread count in the JVM runtime.
 */
public record ThreadTelemetrySnapshot(
        int liveThreadCount,
        int peakThreadCount,
        int daemonThreadCount,
        long totalStartedThreadCount
) {
    public static ThreadTelemetrySnapshot capture() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        return new ThreadTelemetrySnapshot(
                mxBean.getThreadCount(),
                mxBean.getPeakThreadCount(),
                mxBean.getDaemonThreadCount(),
                mxBean.getTotalStartedThreadCount()
        );
    }
}
