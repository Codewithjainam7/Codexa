package com.codexa.analysis.model;

public record LimitsResponse(
        int maxCompressedSizeMb,
        int maxExtractedSizeMb,
        int maxFileCount,
        int maxPathDepth,
        int maxSingleFileSizeMb
) {
}
