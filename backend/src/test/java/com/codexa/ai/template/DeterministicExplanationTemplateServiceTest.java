package com.codexa.ai.template;

import com.codexa.persistence.entity.FindingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicExplanationTemplateServiceTest {

    private DeterministicExplanationTemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new DeterministicExplanationTemplateService();
    }

    @Test
    void shouldGenerateFallbackForNullFieldsGracefully() {
        FindingEntity finding = new FindingEntity();
        var response = templateService.generateFallback(finding);

        assertNotNull(response);
        assertEquals("Code Review Finding: CR-GEN-001", response.title());
        assertNotNull(response.explanation());
        assertNotNull(response.impact());
        assertNotNull(response.remediation());
        assertNotNull(response.suggestedFix());
        assertFalse(response.references().isEmpty());
    }

    @Test
    void shouldPreserveExistingFindingProperties() {
        FindingEntity finding = new FindingEntity();
        finding.setRuleId("CR-SQL-001");
        finding.setTitle("SQL Injection Detection");
        finding.setDescription("Raw concatenation detected.");
        finding.setImpact("Full database takeover possible.");
        finding.setRemediation("Use PreparedStatement.");
        finding.setSuggestedFix("db.find(id);");
        finding.setReferences(List.of("https://cwe.mitre.org/data/definitions/89.html"));
        finding.setRequiresManualReview(true);

        var response = templateService.generateFallback(finding);

        assertEquals("SQL Injection Detection", response.title());
        assertEquals("Raw concatenation detected.", response.explanation());
        assertEquals("Full database takeover possible.", response.impact());
        assertEquals("Use PreparedStatement.", response.remediation());
        assertEquals("db.find(id);", response.suggestedFix());
        assertTrue(response.requiresManualReview());
        assertFalse(response.references().isEmpty());
    }
}
