package com.codexa.ingestion.controller;

import com.codexa.analysis.model.AnalysisJobResponse;
import com.codexa.analysis.model.SourceType;
import com.codexa.analysis.pipeline.AnalysisOrchestrator;
import com.codexa.analysis.service.AnalysisJobService;
import com.codexa.common.error.ApiException;
import com.codexa.ingestion.github.GitHubAnalysisRequest;
import com.codexa.ingestion.github.GitHubIngestionService;
import com.codexa.ingestion.service.StagingManagerService;
import com.codexa.ingestion.zip.ExtractionResult;
import com.codexa.persistence.entity.AnalysisJobEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/analyses/github")
@Tag(name = "Ingestion", description = "Endpoints for submitting codebases for analysis")
public class GitHubAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(GitHubAnalysisController.class);

    private final AnalysisJobService jobService;
    private final StagingManagerService stagingManagerService;
    private final GitHubIngestionService gitHubIngestionService;
    private final AnalysisOrchestrator analysisOrchestrator;

    public GitHubAnalysisController(
            AnalysisJobService jobService,
            StagingManagerService stagingManagerService,
            GitHubIngestionService gitHubIngestionService,
            AnalysisOrchestrator analysisOrchestrator
    ) {
        this.jobService = jobService;
        this.stagingManagerService = stagingManagerService;
        this.gitHubIngestionService = gitHubIngestionService;
        this.analysisOrchestrator = analysisOrchestrator;
    }

    @PostMapping
    @Operation(summary = "Submit public GitHub repository for analysis", description = "Validates public GitHub URL, securely fetches and extracts the repository archive, and stages it for static audit.")
    public ResponseEntity<AnalysisJobResponse> submitGitHubAnalysis(@Valid @RequestBody GitHubAnalysisRequest request) {
        String repoUrl = request.repoUrl().trim();
        log.info("Received analysis request for public GitHub repo: {}", repoUrl);

        // 1. Create Job in database
        AnalysisJobEntity job = jobService.createJob(SourceType.GITHUB, repoUrl);

        Path stagingDir = null;
        try {
            // 2. Prepare staging directory
            stagingDir = stagingManagerService.createStagingDirectory(job.getId());

            // 3. Download & extract archive securely with SSRF & Zip Slip protections
            ExtractionResult extractionResult = gitHubIngestionService.downloadAndExtract(repoUrl, stagingDir);

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
            jobService.markJobFailed(job.getId(), "EXTRACTION_ERROR", "Failed to stage repository: " + e.getMessage());
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "STAGING_FAILED", "Failed to stage repository archive.", e);
        }
    }
}
