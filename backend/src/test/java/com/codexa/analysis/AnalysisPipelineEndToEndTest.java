package com.codexa.analysis;

import com.codexa.analysis.model.ProductionVerdict;
import com.codexa.analysis.pipeline.AnalysisOrchestrator;
import com.codexa.analysis.service.AnalysisJobService;
import com.codexa.ingestion.service.StagingManagerService;
import com.codexa.persistence.entity.AnalysisJobEntity;
import com.codexa.persistence.entity.FindingEntity;
import com.codexa.persistence.repository.FindingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AnalysisPipelineEndToEndTest {

    @Autowired
    private AnalysisOrchestrator orchestrator;

    @Autowired
    private AnalysisJobService jobService;

    @Autowired
    private FindingRepository findingRepository;

    @Autowired
    private StagingManagerService stagingManagerService;

    @Test
    void runFullPipelineOnVulnerableFixtureShouldDetectAllFlawsAndSetNotReady() throws Exception {
        Path vulnerableDir = Paths.get("../fixtures/codexa-demo-vulnerable").toAbsolutePath().normalize();
        if (!Files.exists(vulnerableDir)) {
            vulnerableDir = Paths.get("fixtures/codexa-demo-vulnerable").toAbsolutePath().normalize();
        }

        assertTrue(Files.exists(vulnerableDir), "Fixture directory should exist: " + vulnerableDir);

        List<Path> javaFiles = Files.walk(vulnerableDir)
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());

        assertFalse(javaFiles.isEmpty(), "Fixture must contain Java files");

        UUID jobId = UUID.randomUUID();
        AnalysisJobEntity job = jobService.createJob(com.codexa.analysis.model.SourceType.ZIP, "vulnerable-fixture.zip");

        // Run analysis asynchronously
        orchestrator.runAnalysisAsync(job.getId(), vulnerableDir, javaFiles);

        // Await async completion
        com.codexa.analysis.model.AnalysisJobResponse response = null;
        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            response = jobService.getJobResponse(job.getId());
            if (response.status() == com.codexa.analysis.model.JobStatus.COMPLETED ||
                    response.status() == com.codexa.analysis.model.JobStatus.FAILED) {
                break;
            }
        }

        assertNotNull(response);
        assertEquals(com.codexa.analysis.model.JobStatus.COMPLETED, response.status());
        assertEquals(ProductionVerdict.NOT_READY, response.verdict());
        assertTrue(response.overallScore() < 75.0, "Score should drop significantly due to critical security defects");

        // Verify Findings
        List<FindingEntity> findings = findingRepository.findByJob_IdOrderByPriorityScoreDesc(job.getId());
        assertFalse(findings.isEmpty());

        List<String> detectedRules = findings.stream().map(FindingEntity::getRuleId).toList();
        assertTrue(detectedRules.contains("CR-SQL-001"), "Should detect SQL Injection");
        assertTrue(detectedRules.contains("CR-CMD-001"), "Should detect Command Injection");
        assertTrue(detectedRules.contains("CR-SEC-001"), "Should detect Hardcoded Secrets");
        assertTrue(detectedRules.contains("CR-AUTH-001"), "Should detect Missing Access Control");
        assertTrue(detectedRules.contains("CR-PASS-001"), "Should detect Weak Password Storage");
        assertTrue(detectedRules.contains("CR-CRYPTO-001"), "Should detect Weak Cryptography");
        assertTrue(detectedRules.contains("CR-LOG-001"), "Should detect Sensitive Logging");

        // Verify that secrets in evidence are masked
        FindingEntity secretFinding = findings.stream().filter(f -> f.getRuleId().equals("CR-SEC-001")).findFirst().orElseThrow();
        assertFalse(secretFinding.getEvidenceMasked().contains("super-secret-jwt-key-991283819"));
        assertTrue(secretFinding.getEvidenceMasked().contains("*****"));
    }
}
