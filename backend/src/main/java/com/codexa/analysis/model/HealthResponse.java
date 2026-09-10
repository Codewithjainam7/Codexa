package com.codexa.analysis.model;

import java.time.Instant;

public record HealthResponse(
        String status,
        String appName,
        String version,
        Instant timestamp,
        int ruleCatalogCount
) {
    public HealthResponse(String status, String appName, String version, Instant timestamp) {
        this(status, appName, version, timestamp, 30);
    }

    public static HealthResponse ok() {
        return new HealthResponse("UP", "Codexa Security Platform", "1.0.0", Instant.now(), 30);
    }
}
