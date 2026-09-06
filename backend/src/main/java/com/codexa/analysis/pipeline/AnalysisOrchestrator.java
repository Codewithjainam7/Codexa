package com.codexa.analysis.pipeline;

import com.codexa.analysis.model.JobStatus;
import com.codexa.analysis.model.ProductionVerdict;
import com.codexa.analysis.service.AnalysisJobService;
import com.codexa.ingestion.service.StagingManagerService;
import com.codexa.persistence.entity.FindingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AnalysisOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AnalysisOrchestrator.class);

    private final AnalysisJobService jobService;
    private final StagingManagerService stagingManagerService;
    private final List<PipelineStage> stages;

    public AnalysisOrchestrator(
            AnalysisJobService jobService,
            StagingManagerService stagingManagerService,
            List<PipelineStage> stages
    ) {
        this.jobService = jobService;
        this.stagingManagerService = stagingManagerService;
        this.stages = stages != null ? stages : new ArrayList<>();
    }

    @Async
    public void runAnalysisAsync(UUID jobId, Path stagingDirectory, List<Path> sourceFiles) {
        long startTime = System.currentTimeMillis();
        log.info("Starting asynchronous analysis pipeline for jobId={}", jobId);

        PipelineContext context = new PipelineContext(jobId, stagingDirectory);
        context.setSourceFiles(sourceFiles);
        context.setTotalFiles(sourceFiles != null ? sourceFiles.size() : 0);

        try {
            // Stage: INGESTION / PREPARING
            jobService.updateProgress(jobId, JobStatus.EXTRACTING, "INGESTION", 20);

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
