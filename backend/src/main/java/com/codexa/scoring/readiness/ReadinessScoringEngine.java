package com.codexa.scoring.readiness;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.ProductionVerdict;
import com.codexa.analysis.model.Severity;
import com.codexa.persistence.entity.FindingEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReadinessScoringEngine {

    public record ScoreResult(
            double overallScore,
            double securityScore,
            double qualityScore,
            double operationsScore,
            double maintainabilityScore,
            double architecturalScore,
            ProductionVerdict verdict
    ) {
        public ScoreResult(double overallScore, double securityScore, double qualityScore, double operationsScore, double maintainabilityScore, ProductionVerdict verdict) {
            this(overallScore, securityScore, qualityScore, operationsScore, maintainabilityScore, qualityScore, verdict);
        }

        public ScoreResult(double overallScore, double securityScore, double qualityScore, double operationsScore, ProductionVerdict verdict) {
            this(overallScore, securityScore, qualityScore, operationsScore, qualityScore, qualityScore, verdict);
        }
    }

    private static class PenaltyAccumulator {
        double securityPenalty = 0.0;
        double qualityPenalty = 0.0;
        double operationsPenalty = 0.0;
        boolean hasConfirmedCriticalSecurity = false;
        boolean hasHighAuthOrInjectionOrSecrets = false;
    }

    public ScoreResult computeScores(List<FindingEntity> findings) {
        if (findings == null || findings.isEmpty()) {
            return new ScoreResult(100.0, 100.0, 100.0, 100.0, 100.0, 100.0, ProductionVerdict.REVIEW_COMPLETE);
        }

        PenaltyAccumulator acc = computePenalties(findings);

        double securityScore = Math.max(0.0, 100.0 - acc.securityPenalty);
        double effectiveQualityPenalty = acc.qualityPenalty <= 30.0 ? acc.qualityPenalty : 30.0 + (acc.qualityPenalty - 30.0) * 0.25;
        double qualityScore = Math.round(Math.max(0.0, Math.min(100.0, 100.0 - effectiveQualityPenalty)) * 10.0) / 10.0;

        double effectiveOpsPenalty = acc.operationsPenalty <= 30.0 ? acc.operationsPenalty : 30.0 + (acc.operationsPenalty - 30.0) * 0.3;
        double operationsScore = Math.round(Math.max(0.0, Math.min(100.0, 100.0 - effectiveOpsPenalty)) * 10.0) / 10.0;

        double maintainabilityPenalty = (100.0 - qualityScore) * 0.5 + (100.0 - operationsScore) * 0.3 + Math.min(25.0, findings.size() * 0.2);
        double maintainabilityScore = Math.round(Math.max(0.0, Math.min(100.0, 100.0 - maintainabilityPenalty)) * 10.0) / 10.0;

        double structuralDebt = computeStructuralDebt(findings);
        double architecturalScore = Math.round(Math.max(0.0, Math.min(100.0, 100.0 - Math.min(40.0, structuralDebt) - ((100.0 - qualityScore) * 0.25))) * 10.0) / 10.0;

        double weightedOverall;
        if (acc.hasConfirmedCriticalSecurity) {
            weightedOverall = 0.60 * securityScore + 0.15 * operationsScore;
        } else {
            weightedOverall = 0.60 * securityScore + 0.25 * qualityScore + 0.15 * operationsScore;
        }
        double overallScore = Math.round(weightedOverall * 10.0) / 10.0;

        ProductionVerdict verdict = resolveVerdict(overallScore, acc.hasConfirmedCriticalSecurity, acc.hasHighAuthOrInjectionOrSecrets);

        return new ScoreResult(overallScore, securityScore, qualityScore, operationsScore, maintainabilityScore, architecturalScore, verdict);
    }

    private PenaltyAccumulator computePenalties(List<FindingEntity> findings) {
        PenaltyAccumulator acc = new PenaltyAccumulator();
        for (FindingEntity f : findings) {
            Category cat = f.getCategory() != null ? f.getCategory() : Category.SECURITY;
            Severity sev = f.getSeverity() != null ? f.getSeverity() : Severity.LOW;
            String ruleId = f.getRuleId() != null ? f.getRuleId() : "";

            if (cat == Category.SECURITY) {
                applySecurityPenalty(acc, sev, ruleId);
            } else if (cat == Category.QUALITY) {
                applyQualityPenalty(acc, sev);
            } else if (cat == Category.OPERATIONS) {
                applyOperationsPenalty(acc, sev);
            }
        }
        return acc;
    }

    private void applySecurityPenalty(PenaltyAccumulator acc, Severity sev, String ruleId) {
        if (sev == Severity.CRITICAL) {
            acc.securityPenalty += 30.0;
            acc.hasConfirmedCriticalSecurity = true;
        } else if (sev == Severity.HIGH) {
            acc.securityPenalty += 15.0;
            if (isAuthOrInjectionOrSecretRule(ruleId)) {
                acc.hasHighAuthOrInjectionOrSecrets = true;
            }
        } else if (sev == Severity.MEDIUM) {
            acc.securityPenalty += 8.0;
        } else {
            acc.securityPenalty += 2.0;
        }
    }

    private boolean isAuthOrInjectionOrSecretRule(String ruleId) {
        return ruleId.startsWith("CR-SQL") || ruleId.startsWith("CR-CMD") || ruleId.startsWith("CR-SEC") ||
                ruleId.startsWith("CR-AUTH") || ruleId.startsWith("CR-PASS") || ruleId.startsWith("CR-LEAK") ||
                ruleId.startsWith("CR-EDGE") || ruleId.startsWith("CR-RLS") || ruleId.startsWith("CR-PARAM");
    }

    private void applyQualityPenalty(PenaltyAccumulator acc, Severity sev) {
        if (sev == Severity.HIGH) acc.qualityPenalty += 12.0;
        else if (sev == Severity.MEDIUM) acc.qualityPenalty += 3.0;
        else acc.qualityPenalty += 0.5;
    }

    private void applyOperationsPenalty(PenaltyAccumulator acc, Severity sev) {
        if (sev == Severity.HIGH) acc.operationsPenalty += 12.0;
        else if (sev == Severity.MEDIUM) acc.operationsPenalty += 4.0;
        else acc.operationsPenalty += 1.0;
    }

    private double computeStructuralDebt(List<FindingEntity> findings) {
        double debt = 0.0;
        for (FindingEntity f : findings) {
            String ruleId = f.getRuleId() != null ? f.getRuleId() : "";
            if (ruleId.startsWith("CR-ARCH") || ruleId.startsWith("CR-COMPLEX") || ruleId.startsWith("CR-NEST") || ruleId.startsWith("CR-DUP") || ruleId.startsWith("CR-QUAL-001")) {
                debt += 1.5;
            }
        }
        return debt;
    }

    private ProductionVerdict resolveVerdict(double overallScore, boolean hasConfirmedCritical, boolean hasHighAuthSecrets) {
        // Critical Caps per specification Section 10
        if (hasConfirmedCritical) {
            return ProductionVerdict.NOT_READY;
        }
        if (hasHighAuthSecrets) {
            return overallScore >= 50 ? ProductionVerdict.NEEDS_URGENT_FIXES : ProductionVerdict.NOT_READY;
        }

        if (overallScore >= 90.0) {
            return ProductionVerdict.REVIEW_COMPLETE;
        } else if (overallScore >= 75.0) {
            return ProductionVerdict.GENERALLY_PROMISING;
        } else if (overallScore >= 50.0) {
            return ProductionVerdict.NEEDS_URGENT_FIXES;
        } else {
            return ProductionVerdict.NOT_READY;
        }
    }

    public static String getQualityRating(double score) {
        if (score >= 90.0) return "EXCELLENT";
        if (score >= 75.0) return "GOOD";
        if (score >= 50.0) return "FAIR";
        return "CRITICAL_ATTENTION_REQUIRED";
    }

    public static String getSecurityRating(double score) {
        if (score >= 90.0) return "HARDENED";
        if (score >= 75.0) return "ACCEPTABLE";
        if (score >= 50.0) return "AT_RISK";
        return "VULNERABLE";
    }

    public static double calculateDebtRatio(int totalFindings, int totalFiles) {
        if (totalFiles <= 0) return 0.0;
        double ratio = (double) totalFindings / totalFiles;
        return Math.round(ratio * 100.0) / 100.0;
    }
}
