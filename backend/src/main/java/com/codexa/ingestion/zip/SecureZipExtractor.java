package com.codexa.ingestion.zip;

import com.codexa.common.error.ApiException;
import com.codexa.config.CodexaProperties;
import com.codexa.ingestion.service.FileFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class SecureZipExtractor {

    private static final Logger log = LoggerFactory.getLogger(SecureZipExtractor.class);

    private final CodexaProperties properties;
    private final FileFilterService fileFilterService;

    public SecureZipExtractor(CodexaProperties properties, FileFilterService fileFilterService) {
        this.properties = properties;
        this.fileFilterService = fileFilterService;
    }

    public ExtractionResult extract(InputStream zipStream, Path targetDir) throws IOException {
        CodexaProperties.Limits limits = properties.limits();
        Path normalizedTargetDir = targetDir.toAbsolutePath().normalize();

        int fileCount = 0;
        long totalBytesExtracted = 0;
        List<Path> extractedFiles = new ArrayList<>();

        // 64 KB buffer optimized for high-throughput enterprise archive decompression
        byte[] buffer = new byte[65536];

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipStream))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                // 1. Sanitize & Zip Slip Check
                Path entryDestination = normalizedTargetDir.resolve(entryName).normalize();
                if (!entryDestination.startsWith(normalizedTargetDir)) {
                    log.warn("Zip Slip directory traversal detected in entry: {}", entryName);
                    throw new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "ZIP_SLIP_DETECTED",
                            "Archive contains invalid path sequence traversing outside the target staging directory."
                    );
                }

                // 2. Path Depth Check
                Path relativeToTarget = normalizedTargetDir.relativize(entryDestination);
                if (relativeToTarget.getNameCount() > limits.maxPathDepth()) {
                    log.warn("Exceeded max directory path depth ({}) at: {}", limits.maxPathDepth(), entryName);
                    throw new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "ZIP_DEPTH_EXCEEDED",
                            "Archive exceeds maximum directory nesting depth of " + limits.maxPathDepth()
                    );
                }

                // 3. Skip ignored directories
                if (fileFilterService.isIgnoredDirectory(relativeToTarget)) {
                    zis.closeEntry();
                    continue;
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryDestination);
                    zis.closeEntry();
                    continue;
                }

                // 4. Check if file is of supported type
                String fileName = entryDestination.getFileName().toString();
                if (!fileFilterService.isSupportedAnalysisFile(fileName)) {
                    zis.closeEntry();
                    continue;
                }

                // 5. File Count Limit Check (Zip Bomb protection)
                fileCount++;
                if (fileCount > limits.maxFileCount()) {
                    log.warn("Zip bomb detected: exceeded file count limit of {}", limits.maxFileCount());
                    throw new ApiException(
                            HttpStatus.PAYLOAD_TOO_LARGE,
                            "ZIP_BOMB_FILE_COUNT_EXCEEDED",
                            "Archive exceeds maximum allowed file count of " + limits.maxFileCount()
                    );
                }

                // Ensure parent directory exists
                if (entryDestination.getParent() != null) {
                    Files.createDirectories(entryDestination.getParent());
                }

                // 6. Write file with per-file and total-size bounds
                long singleFileBytes = 0;
                try (var fos = Files.newOutputStream(entryDestination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    int bytesRead;
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        singleFileBytes += bytesRead;
                        totalBytesExtracted += bytesRead;

                        if (singleFileBytes > limits.maxSingleFileSizeBytes()) {
                            throw new ApiException(
                                    HttpStatus.PAYLOAD_TOO_LARGE,
                                    "ZIP_SINGLE_FILE_TOO_LARGE",
                                    "Extracted file '" + entryName + "' exceeds limit of " + limits.maxSingleFileSizeMb() + " MB"
                            );
                        }

                        if (totalBytesExtracted > limits.maxExtractedSizeBytes()) {
                            throw new ApiException(
                                    HttpStatus.PAYLOAD_TOO_LARGE,
                                    "ZIP_BOMB_TOTAL_SIZE_EXCEEDED",
                                    "Archive total extracted size exceeds limit of " + limits.maxExtractedSizeMb() + " MB"
                            );
                        }

                        fos.write(buffer, 0, bytesRead);
                    }
                }

                extractedFiles.add(entryDestination);
                zis.closeEntry();
            }
        }

        if (extractedFiles.isEmpty()) {
            log.info("No supported analysis files extracted from archive.");
        } else {
            log.info("Successfully extracted {} files ({} bytes) into {}", fileCount, totalBytesExtracted, normalizedTargetDir);
        }

        return new ExtractionResult(normalizedTargetDir, fileCount, totalBytesExtracted, extractedFiles);
    }
}
