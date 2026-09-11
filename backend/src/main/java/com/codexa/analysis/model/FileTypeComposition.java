package com.codexa.analysis.model;

import java.util.Collections;
import java.util.Map;

/**
 * Statistical distribution of repository source file extensions and lines of code.
 */
public record FileTypeComposition(
        int totalFiles,
        int totalCodeLines,
        int totalCommentLines,
        int totalBlankLines,
        Map<String, Integer> fileCountsByExtension,
        Map<String, Integer> locByLanguage
) {
    public static FileTypeComposition empty() {
        return new FileTypeComposition(0, 0, 0, 0, Collections.emptyMap(), Collections.emptyMap());
    }
}
