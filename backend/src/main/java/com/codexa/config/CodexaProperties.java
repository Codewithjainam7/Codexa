package com.codexa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "codexa")
public record CodexaProperties(
        Limits limits,
        Staging staging,
        Ai ai,
        Security security
) {
    @ConstructorBinding
    public CodexaProperties(Limits limits, Staging staging, Ai ai, Security security) {
        this.limits = limits != null ? limits : new Limits(25, 100, 1000, 15, 5);
        this.staging = staging != null ? staging : new Staging(".staging", true);
        this.ai = ai != null ? ai : new Ai(false, "none", "", "nvidia/llama-3.1-nemotron-70b-instruct", 20000);
        this.security = security != null ? security : new Security(true, 60);
    }

    public CodexaProperties(Limits limits, Staging staging, Ai ai) {
        this(limits, staging, ai, new Security(true, 60));
    }
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
            if (model == null) model = "nvidia/llama-3.1-nemotron-70b-instruct";
            if (timeoutMs <= 0) timeoutMs = 20000;
        }
    }

    public record Security(
            boolean rateLimitEnabled,
            int rateLimitRequestsPerMinute
    ) {
        public Security {
            if (rateLimitRequestsPerMinute <= 0) rateLimitRequestsPerMinute = 60;
        }
    }
}
