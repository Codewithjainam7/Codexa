package com.codexa.analysis.pipeline;

import com.codexa.analysis.model.JobStatus;
import com.codexa.analysis.model.ProductionVerdict;
import com.codexa.analysis.service.AnalysisJobService;
import com.codexa.analysis.service.ProjectDiagnosticsCollector;
import com.codexa.common.error.ApiException;
import com.codexa.ingestion.github.GitHubIngestionService;
import com.codexa.ingestion.service.StagingManagerService;
import com.codexa.ingestion.zip.ExtractionResult;
import com.codexa.persistence.entity.FindingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnalysisOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AnalysisOrchestrator.class);

    private final AnalysisJobService jobService;
    private final StagingManagerService stagingManagerService;
    private final ProjectDiagnosticsCollector diagnosticsCollector;
    private final GitHubIngestionService gitHubIngestionService;
    private final List<PipelineStage> stages;

    @org.springframework.beans.factory.annotation.Autowired
    public AnalysisOrchestrator(
            AnalysisJobService jobService,
            StagingManagerService stagingManagerService,
            ProjectDiagnosticsCollector diagnosticsCollector,
            GitHubIngestionService gitHubIngestionService,
            List<PipelineStage> stages
    ) {
        this.jobService = jobService;
        this.stagingManagerService = stagingManagerService;
        this.diagnosticsCollector = diagnosticsCollector;
        this.gitHubIngestionService = gitHubIngestionService;
        this.stages = stages != null ? stages : new ArrayList<>();
    }

    public AnalysisOrchestrator(
            AnalysisJobService jobService,
            StagingManagerService stagingManagerService,
            ProjectDiagnosticsCollector diagnosticsCollector,
            List<PipelineStage> stages
    ) {
        this(jobService, stagingManagerService, diagnosticsCollector, null, stages);
    }

    public AnalysisOrchestrator(
            AnalysisJobService jobService,
            StagingManagerService stagingManagerService,
            List<PipelineStage> stages
    ) {
        this(jobService, stagingManagerService, new ProjectDiagnosticsCollector(), null, stages);
    }

    @Async
    public void runGitHubAnalysisAsync(UUID jobId, String repoUrl) {
        long startTime = System.currentTimeMillis();
        log.info("Starting asynchronous GitHub download & analysis for jobId={}, repoUrl={}", jobId, repoUrl);

        Path stagingDir = null;
        try {
            // Stage 1: Fast initial progress update
            jobService.updateProgress(jobId, JobStatus.EXTRACTING, "DOWNLOADING_REPOSITORY", 10);

            // Stage 2: Create isolated staging directory
            stagingDir = stagingManagerService.createStagingDirectory(jobId);

            if (gitHubIngestionService == null) {
                throw new IllegalStateException("GitHubIngestionService is not configured");
            }

            // Stage 3: Download & extract archive securely with SSRF & Zip Slip protections
            ExtractionResult extractionResult = gitHubIngestionService.downloadAndExtract(repoUrl, stagingDir);

            jobService.updateProgress(jobId, JobStatus.EXTRACTING, "ARCHIVE_UNPACKED", 25);

            // Stage 4: Run the static audit pipeline stages
            executePipeline(jobId, stagingDir, extractionResult.extractedSourceFiles(), startTime);

        } catch (ApiException e) {
            log.error("GitHub ingestion rejected for jobId={}: [{}] {}", jobId, e.getErrorCode(), e.getMessage());
            if (stagingDir != null) {
                stagingManagerService.cleanDirectory(stagingDir);
            }
            jobService.markJobFailed(jobId, e.getErrorCode(), e.getMessage());
        } catch (IOException e) {
            log.error("Staging extraction failed for jobId={}: {}", jobId, e.getMessage(), e);
            if (stagingDir != null) {
                stagingManagerService.cleanDirectory(stagingDir);
            }
            jobService.markJobFailed(jobId, "EXTRACTION_ERROR", "Failed to stage repository: " + e.getMessage());
        } catch (Exception e) {
            log.error("Analysis pipeline failed unexpectedly for jobId={}: {}", jobId, e.getMessage(), e);
            if (stagingDir != null) {
                stagingManagerService.cleanDirectory(stagingDir);
            }
            jobService.markJobFailed(jobId, "PIPELINE_ERROR", "Unexpected analysis error: " + e.getMessage());
        }
    }

    @Async
    public void runAnalysisAsync(UUID jobId, Path stagingDirectory, List<Path> sourceFiles) {
        long startTime = System.currentTimeMillis();
        log.info("Starting asynchronous analysis pipeline for jobId={}", jobId);
        jobService.updateProgress(jobId, JobStatus.EXTRACTING, "INGESTION", 20);
        executePipeline(jobId, stagingDirectory, sourceFiles, startTime);
    }

    private void executePipeline(UUID jobId, Path stagingDirectory, List<Path> sourceFiles, long startTime) {
        PipelineContext context = new PipelineContext(jobId, stagingDirectory);
        context.setSourceFiles(sourceFiles);
        context.setTotalFiles(sourceFiles != null ? sourceFiles.size() : 0);

        try {
            // Execute all configured pipeline stages
            for (PipelineStage stage : stages) {
                String stageName = stage.getStageName();
                int percent = switch (stageName) {
                    case "JAVA_AST_PARSING" -> 40;
                    case "SECURITY_AND_QUALITY_RULES" -> 65;
                    case "AI_EXPLANATION_AND_REMEDIATION" -> 85;
                    case "PRIORITIZATION_AND_SCORING" -> 95;
                    default -> 70;
                };
                jobService.updateProgress(jobId, JobStatus.SCANNING, stageName, percent);
                log.debug("Executing pipeline stage [{}] for jobId={}", stageName, jobId);
                stage.execute(context);
            }

            // Collect Project Diagnostics
            try {
                com.codexa.analysis.model.ProjectDiagnostics diag = diagnosticsCollector.collect(context);
                context.setProjectDiagnostics(diag);
                jobService.saveDiagnostics(jobId, diag);
            } catch (Exception ex) {
                log.warn("Failed to collect detailed project diagnostics for jobId={}: {}", jobId, ex.getMessage());
            }

            // Stage: COMPLETING & SCORING
            jobService.updateProgress(jobId, JobStatus.PRIORITIZING, "PRIORITIZATION_AND_SCORING", 98);

            long durationMs = System.currentTimeMillis() - startTime;
            jobService.completeJob(
                    jobId,
                    context.getOverallScore(),
                    context.getVerdict(),
                    context.getExecutiveSummary().isBlank() ? "Analysis completed successfully." : context.getExecutiveSummary(),
                    context.getFindings(),
                    context.getSecurityScore(),
                    context.getQualityScore(),
                    context.getOperationsScore(),
                    context.getMaintainabilityScore(),
                    context.getArchitecturalScore(),
                    context.getTotalFiles(),
                    context.getAnalyzedFiles(),
                    durationMs
            );

            log.info("Pipeline completed successfully for jobId={} in {}ms with score={}",
                    jobId, durationMs, context.getOverallScore());

        } catch (Exception ex) {
            log.error("Analysis pipeline failed for jobId={}: {}", jobId, ex.getMessage(), ex);
            jobService.markJobFailed(jobId, "PIPELINE_ERROR", ex.getMessage());
        } finally {
            if (stagingManagerService.isCleanupOnCompletion()) {
                stagingManagerService.cleanDirectory(stagingDirectory);
            }
        }
    }
}
