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
        this.limits = limits != null ? limits : new Limits(500, 1000, 20000, 30, 100);
        this.staging = staging != null ? staging : new Staging(".staging", true);
        this.ai = ai != null ? ai : new Ai(true, "openrouter", "", "anthropic/claude-fable-5.1", "nvidia/nemotron-3-ultra-550b-a55b:free", "https://openrouter.ai/api/v1/chat/completions", 30000);
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
            if (maxCompressedSizeMb <= 0) maxCompressedSizeMb = 500;
            if (maxExtractedSizeMb <= 0) maxExtractedSizeMb = 1000;
            if (maxFileCount <= 0) maxFileCount = 20000;
            if (maxPathDepth <= 0) maxPathDepth = 30;
            if (maxSingleFileSizeMb <= 0) maxSingleFileSizeMb = 100;
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
            String fallbackModel,
            String endpoint,
            int timeoutMs
    ) {
        public Ai {
            if (provider == null) provider = "openrouter";
            if (model == null || model.isBlank()) model = "anthropic/claude-fable-5.1";
            if (fallbackModel == null || fallbackModel.isBlank()) fallbackModel = "nvidia/nemotron-3-ultra-550b-a55b:free";
            if (endpoint == null || endpoint.isBlank()) endpoint = "https://openrouter.ai/api/v1/chat/completions";
            if (timeoutMs <= 0) timeoutMs = 30000;
        }

        public Ai(boolean enabled, String provider, String apiKey, String model, int timeoutMs) {
            this(enabled, provider, apiKey, model, "nvidia/nemotron-3-ultra-550b-a55b:free", "https://openrouter.ai/api/v1/chat/completions", timeoutMs);
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
