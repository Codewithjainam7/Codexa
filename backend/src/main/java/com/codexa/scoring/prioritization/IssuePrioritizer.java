package com.codexa.scoring.prioritization;

import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.persistence.entity.FindingEntity;
import org.springframework.stereotype.Component;

@Component
public class IssuePrioritizer {

    public double calculatePriority(FindingEntity finding) {
        double severityWeight = getSeverityWeight(finding.getSeverity());
        double confidenceWeight = getConfidenceWeight(finding.getConfidence());
        double exposureWeight = getExposureWeight(finding);
        double impactWeight = getImpactWeight(finding);

        double rawPriority = severityWeight * confidenceWeight * exposureWeight * impactWeight;
        // Round to 3 decimal places
        return Math.round(rawPriority * 1000.0) / 1000.0;
    }

    private double getSeverityWeight(Severity severity) {
        if (severity == null) return 0.25;
        return switch (severity) {
            case CRITICAL -> 1.00;
            case HIGH -> 0.80;
            case MEDIUM -> 0.55;
            case LOW -> 0.25;
            case INFO -> 0.10;
        };
    }

    private double getConfidenceWeight(Confidence confidence) {
        if (confidence == null) return 0.50;
        return switch (confidence) {
            case HIGH -> 1.00;
            case MEDIUM -> 0.75;
            case LOW -> 0.50;
        };
    }

    private double getExposureWeight(FindingEntity finding) {
        String path = (finding.getFilePath() != null ? finding.getFilePath() : "").toLowerCase();
        String desc = (finding.getDescription() != null ? finding.getDescription() : "").toLowerCase();

        if (path.contains("controller") || desc.contains("endpoint") || desc.contains("cors") || desc.contains("http")) {
            return 1.00; // public endpoint exposure
        }
        if (path.contains("service") || path.contains("auth")) {
            return 0.75; // internal authenticated service
        }
        return 0.50; // internal / unknown
    }

    private double getImpactWeight(FindingEntity finding) {
        String ruleId = finding.getRuleId() != null ? finding.getRuleId() : "";
        if (ruleId.startsWith("CR-SQL") || ruleId.startsWith("CR-CMD") || ruleId.startsWith("CR-SEC") ||
                ruleId.startsWith("CR-AUTH") || ruleId.startsWith("CR-PASS") || ruleId.startsWith("CR-CRYPTO")) {
            return 1.00; // auth / secrets / payment / PII impact
        }
        if (ruleId.startsWith("CR-XSS") || ruleId.startsWith("CR-DEP") || ruleId.startsWith("CR-QUAL-001")) {
            return 0.70; // normal business data impact
        }
        return 0.40; // low impact
    }
}
