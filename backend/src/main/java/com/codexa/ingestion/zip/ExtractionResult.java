package com.codexa.ingestion.zip;

import java.nio.file.Path;
import java.util.List;

public record ExtractionResult(
        Path stagingDirectory,
        int totalFilesExtracted,
        long totalBytesExtracted,
        List<Path> extractedSourceFiles
) {
}
