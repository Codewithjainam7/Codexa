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
            // Stage: EXTRACTING / PREPARING
            jobService.updateProgress(jobId, JobStatus.EXTRACTING, "EXTRACTING", 20);

            // Execute all configured pipeline stages
            int totalStages = Math.max(stages.size(), 1);
            int current = 0;

            for (PipelineStage stage : stages) {
                current++;
                int percent = 20 + (int) (((double) current / totalStages) * 60);
                jobService.updateProgress(jobId, JobStatus.SCANNING, stage.getStageName(), percent);
                log.debug("Executing pipeline stage [{}] for jobId={}", stage.getStageName(), jobId);
                stage.execute(context);
            }

            // Stage: COMPLETING & SCORING
            jobService.updateProgress(jobId, JobStatus.PRIORITIZING, "SCORING", 90);

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
