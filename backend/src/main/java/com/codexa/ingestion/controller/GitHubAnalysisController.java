package com.codexa.ingestion.controller;

import com.codexa.analysis.model.AnalysisJobResponse;
import com.codexa.analysis.model.JobStatus;
import com.codexa.analysis.model.SourceType;
import com.codexa.analysis.pipeline.AnalysisOrchestrator;
import com.codexa.analysis.service.AnalysisJobService;
import com.codexa.ingestion.github.GitHubAnalysisRequest;
import com.codexa.ingestion.github.GitHubIngestionService;
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

@RestController
@RequestMapping("/api/v1/analyses/github")
@Tag(name = "Ingestion", description = "Endpoints for submitting codebases for analysis")
public class GitHubAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(GitHubAnalysisController.class);

    private final AnalysisJobService jobService;
    private final GitHubIngestionService gitHubIngestionService;
    private final AnalysisOrchestrator analysisOrchestrator;

    public GitHubAnalysisController(
            AnalysisJobService jobService,
            GitHubIngestionService gitHubIngestionService,
            AnalysisOrchestrator analysisOrchestrator
    ) {
        this.jobService = jobService;
        this.gitHubIngestionService = gitHubIngestionService;
        this.analysisOrchestrator = analysisOrchestrator;
    }

    @PostMapping
    @Operation(summary = "Submit public GitHub repository for analysis", description = "Validates public GitHub URL, creates analysis job, and asynchronously downloads, extracts, and audits the repository.")
    public ResponseEntity<AnalysisJobResponse> submitGitHubAnalysis(@Valid @RequestBody GitHubAnalysisRequest request) {
        String repoUrl = request.repoUrl().trim();
        log.info("Received analysis request for public GitHub repo: {}", repoUrl);

        // 1. Fast URL format validation (throws 400 ApiException if invalid)
        gitHubIngestionService.validateUrl(repoUrl);

        // 2. Create Job in database
        AnalysisJobEntity job = jobService.createJob(SourceType.GITHUB, repoUrl);
        jobService.updateProgress(job.getId(), JobStatus.EXTRACTING, "QUEUED_FOR_DOWNLOAD", 5);

        // 3. Launch download, extraction, and static audit pipeline asynchronously
        analysisOrchestrator.runGitHubAnalysisAsync(job.getId(), repoUrl);

        // 4. Return 202 Accepted immediately (< 15ms)
        AnalysisJobResponse response = jobService.getJobResponse(job.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
