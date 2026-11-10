package com.codexa.analysis.model;

/**
 * Structural Information Flow Metrics (Henry & Kafura, 1981).
 */
public record FanMetrics(
        String entityName,
        int fanIn,
        int fanOut,
        int linesOfCode,
        double structuralComplexity,
        boolean isBottleneck
) {}
