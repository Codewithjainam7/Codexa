package com.codexa.scoring.readiness;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.ProductionVerdict;
import com.codexa.analysis.model.Severity;
import com.codexa.persistence.entity.FindingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReadinessScoringEngineTest {

    private ReadinessScoringEngine scoringEngine;

    @BeforeEach
    void setUp() {
        scoringEngine = new ReadinessScoringEngine();
    }

    @Test
    void cleanScanShouldReturn100AndReviewComplete() {
        var result = scoringEngine.computeScores(List.of());
        assertEquals(100.0, result.overallScore());
        assertEquals(ProductionVerdict.REVIEW_COMPLETE, result.verdict());
    }

    @Test
    void criticalSecurityFindingMustCapVerdictAtNotReady() {
        FindingEntity critical = new FindingEntity();
        critical.setRuleId("CR-SQL-001");
        critical.setCategory(Category.SECURITY);
        critical.setSeverity(Severity.CRITICAL);

        var result = scoringEngine.computeScores(List.of(critical));
        assertEquals(ProductionVerdict.NOT_READY, result.verdict());
    }

    @Test
    void highSecurityFindingMustCapVerdictAtNeedsUrgentFixes() {
        FindingEntity high = new FindingEntity();
        high.setRuleId("CR-SEC-001");
        high.setCategory(Category.SECURITY);
        high.setSeverity(Severity.HIGH);

        var result = scoringEngine.computeScores(List.of(high));
        assertEquals(ProductionVerdict.NEEDS_URGENT_FIXES, result.verdict());
    }
}
