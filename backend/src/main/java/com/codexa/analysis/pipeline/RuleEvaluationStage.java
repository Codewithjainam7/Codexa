package com.codexa.analysis.pipeline;

import com.codexa.persistence.entity.FindingEntity;
import com.codexa.rules.service.RuleEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(20)
public class RuleEvaluationStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationStage.class);

    private final RuleEngineService ruleEngineService;

    public RuleEvaluationStage(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }

    @Override
    public String getStageName() {
        return "SECURITY_AND_QUALITY_RULES";
    }

    @Override
    public void execute(PipelineContext context) {
        log.info("Executing Security and Quality Rules evaluation for jobId={}", context.getJobId());

        List<FindingEntity> findings = ruleEngineService.analyze(context);
        context.setFindings(findings);

        log.info("Rule evaluation complete for jobId={}: identified {} potential findings.",
                context.getJobId(), findings.size());
    }
}
