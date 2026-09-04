package com.codexa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "codexa")
public record CodexaProperties(
        Limits limits,
        Staging staging,
        Ai ai
) {
    public record Limits(
            int maxCompressedSizeMb,
            int maxExtractedSizeMb,
            int maxFileCount,
            int maxPathDepth,
            int maxSingleFileSizeMb
    ) {
        public Limits {
            if (maxCompressedSizeMb <= 0) maxCompressedSizeMb = 25;
            if (maxExtractedSizeMb <= 0) maxExtractedSizeMb = 100;
            if (maxFileCount <= 0) maxFileCount = 1000;
            if (maxPathDepth <= 0) maxPathDepth = 15;
            if (maxSingleFileSizeMb <= 0) maxSingleFileSizeMb = 5;
        }

        public long maxCompressedSizeBytes() {
            return (long) maxCompressedSizeMb * 1024 * 1024;
        }

        public long maxExtractedSizeBytes() {
            return (long) maxExtractedSizeMb * 1024 * 1024;
        }

        public long maxSingleFileSizeBytes() {
            return (long) maxSingleFileSizeMb * 1024 * 1024;
        }
    }

    public record Staging(
            String baseDir,
            boolean cleanupOnCompletion
    ) {
        public Staging {
            if (baseDir == null || baseDir.isBlank()) baseDir = ".staging";
        }
    }

    public record Ai(
            boolean enabled,
            String provider,
            String apiKey,
            String model,
            int timeoutMs
    ) {
        public Ai {
            if (provider == null) provider = "none";
            if (model == null) model = "gemini-1.5-pro";
            if (timeoutMs <= 0) timeoutMs = 15000;
        }
    }
}
