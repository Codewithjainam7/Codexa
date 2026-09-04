package com.codexa.analysis.pipeline;

import com.codexa.persistence.entity.FindingEntity;
import com.codexa.scoring.prioritization.IssuePrioritizer;
import com.codexa.scoring.readiness.ReadinessScoringEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@Order(40)
public class PrioritizationAndScoringStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(PrioritizationAndScoringStage.class);

    private final IssuePrioritizer prioritizer;
    private final ReadinessScoringEngine scoringEngine;

    public PrioritizationAndScoringStage(
            IssuePrioritizer prioritizer,
            ReadinessScoringEngine scoringEngine
    ) {
        this.prioritizer = prioritizer;
        this.scoringEngine = scoringEngine;
    }

    @Override
    public String getStageName() {
        return "PRIORITIZATION_AND_SCORING";
    }

    @Override
    public void execute(PipelineContext context) {
        log.info("Executing Prioritization and Production Readiness Scoring for jobId={}", context.getJobId());

        List<FindingEntity> findings = context.getFindings();

        // 1. Calculate Priority Score for each finding
        for (FindingEntity finding : findings) {
            double priority = prioritizer.calculatePriority(finding);
            finding.setPriorityScore(priority);
        }

        // 2. Sort by highest priority first
        findings.sort(Comparator.comparingDouble(FindingEntity::getPriorityScore).reversed());

        // 3. Compute 0–100 Scores & Verdict
        ReadinessScoringEngine.ScoreResult result = scoringEngine.computeScores(findings);

        context.setOverallScore(result.overallScore());
        context.setSecurityScore(result.securityScore());
        context.setQualityScore(result.qualityScore());
        context.setOperationsScore(result.operationsScore());
        context.setVerdict(result.verdict());

        log.info("Scoring complete for jobId={}: Overall={}/100 (Security={}, Quality={}, Ops={}), Verdict={}",
                context.getJobId(), result.overallScore(), result.securityScore(),
                result.qualityScore(), result.operationsScore(), result.verdict());
    }
}
