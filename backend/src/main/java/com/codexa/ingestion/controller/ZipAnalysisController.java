package com.codexa.ingestion.controller;

import com.codexa.analysis.model.AnalysisJobResponse;
import com.codexa.analysis.model.SourceType;
import com.codexa.analysis.pipeline.AnalysisOrchestrator;
import com.codexa.analysis.service.AnalysisJobService;
import com.codexa.common.error.ApiException;
import com.codexa.config.CodexaProperties;
import com.codexa.ingestion.service.StagingManagerService;
import com.codexa.ingestion.zip.ExtractionResult;
import com.codexa.ingestion.zip.SecureZipExtractor;
import com.codexa.persistence.entity.AnalysisJobEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/analyses/zip")
@Tag(name = "Ingestion", description = "Endpoints for submitting codebases for analysis")
public class ZipAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(ZipAnalysisController.class);

    private final CodexaProperties properties;
    private final AnalysisJobService jobService;
    private final StagingManagerService stagingManagerService;
    private final SecureZipExtractor zipExtractor;
    private final AnalysisOrchestrator analysisOrchestrator;

    public ZipAnalysisController(
            CodexaProperties properties,
            AnalysisJobService jobService,
            StagingManagerService stagingManagerService,
            SecureZipExtractor zipExtractor,
            AnalysisOrchestrator analysisOrchestrator
    ) {
        this.properties = properties;
        this.jobService = jobService;
        this.stagingManagerService = stagingManagerService;
        this.zipExtractor = zipExtractor;
        this.analysisOrchestrator = analysisOrchestrator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit ZIP archive for analysis", description = "Securely uploads and extracts a ZIP archive, staging it for static security and quality inspection.")
    public ResponseEntity<AnalysisJobResponse> submitZipAnalysis(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "Uploaded ZIP file is empty or missing.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "Only .zip archive format is supported.");
        }

        long maxCompressedBytes = properties.limits().maxCompressedSizeBytes();
        if (file.getSize() > maxCompressedBytes) {
            throw new ApiException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "FILE_TOO_LARGE",
                    "Uploaded archive size (" + (file.getSize() / (1024 * 1024)) + " MB) exceeds maximum allowed limit of "
                            + properties.limits().maxCompressedSizeMb() + " MB"
            );
        }

        // 1. Create Job in database
        AnalysisJobEntity job = jobService.createJob(SourceType.ZIP, originalFilename);
        log.info("Created analysis job {} for uploaded file '{}' ({} bytes)", job.getId(), originalFilename, file.getSize());

        Path stagingDir = null;
        try {
            // 2. Prepare staging directory
            stagingDir = stagingManagerService.createStagingDirectory(job.getId());

            // 3. Extract safely with Zip Slip & bomb limits
            ExtractionResult extractionResult = zipExtractor.extract(file.getInputStream(), stagingDir);

            // 4. Asynchronously launch analysis pipeline
            analysisOrchestrator.runAnalysisAsync(job.getId(), stagingDir, extractionResult.extractedSourceFiles());

            // 5. Return 202 Accepted
            AnalysisJobResponse response = jobService.getJobResponse(job.getId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (ApiException e) {
            if (stagingDir != null) {
                stagingManagerService.cleanDirectory(stagingDir);
            }
            jobService.markJobFailed(job.getId(), e.getErrorCode(), e.getMessage());
            throw e;
        } catch (IOException e) {
            if (stagingDir != null) {
                stagingManagerService.cleanDirectory(stagingDir);
            }
            jobService.markJobFailed(job.getId(), "EXTRACTION_ERROR", "Failed to extract ZIP archive: " + e.getMessage());
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "EXTRACTION_FAILED", "Failed to extract ZIP archive safely.", e);
        }
    }
}
