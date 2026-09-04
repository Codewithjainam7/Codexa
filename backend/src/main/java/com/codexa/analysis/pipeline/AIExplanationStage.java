package com.codexa.analysis.pipeline;

import com.codexa.ai.service.AIExplanationService;
import com.codexa.persistence.entity.FindingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
public class AIExplanationStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(AIExplanationStage.class);

    private final AIExplanationService aiExplanationService;

    public AIExplanationStage(AIExplanationService aiExplanationService) {
        this.aiExplanationService = aiExplanationService;
    }

    @Override
    public String getStageName() {
        return "AI_EXPLANATION_AND_REMEDIATION";
    }

    @Override
    public void execute(PipelineContext context) {
        log.info("Executing AI Explanation & Remediation stage for jobId={}", context.getJobId());

        for (FindingEntity finding : context.getFindings()) {
            try {
                aiExplanationService.enrichFinding(finding);
            } catch (Exception e) {
                log.warn("Failed to enrich finding {} with AI explanation: {}", finding.getRuleId(), e.getMessage());
            }
        }

        log.info("AI explanations and code fixes completed for {} findings.", context.getFindings().size());
    }
}
