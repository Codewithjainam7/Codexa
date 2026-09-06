package com.codexa.ai.service;

import com.codexa.ai.client.NvidiaNemotronAiClient;
import com.codexa.ai.mask.SecretMasker;
import com.codexa.ai.model.LlmExplanationResponse;
import com.codexa.ai.template.DeterministicExplanationTemplateService;
import com.codexa.persistence.entity.FindingEntity;
import com.codexa.persistence.entity.LlmCacheEntity;
import com.codexa.persistence.repository.LlmCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AIExplanationService {

    private static final Logger log = LoggerFactory.getLogger(AIExplanationService.class);

    private final SecretMasker secretMasker;
    private final NvidiaNemotronAiClient nemotronAiClient;
    private final DeterministicExplanationTemplateService templateService;
    private final LlmCacheRepository cacheRepository;
    private final ObjectMapper objectMapper;

    public AIExplanationService(
            SecretMasker secretMasker,
            NvidiaNemotronAiClient nemotronAiClient,
            DeterministicExplanationTemplateService templateService,
            LlmCacheRepository cacheRepository,
            ObjectMapper objectMapper
    ) {
        this.secretMasker = secretMasker;
        this.nemotronAiClient = nemotronAiClient;
        this.templateService = templateService;
        this.cacheRepository = cacheRepository;
        this.objectMapper = objectMapper;
    }

    public void enrichFinding(FindingEntity finding) {
        String maskedEvidence = secretMasker.mask(finding.getEvidenceMasked());
        finding.setEvidenceMasked(maskedEvidence);

        String cacheKey = computeCacheKey(finding.getRuleId(), maskedEvidence, finding.getRemediation());

        // 1. Check local cache
        Optional<LlmCacheEntity> cached = cacheRepository.findByCacheKey(cacheKey);
        if (cached.isPresent()) {
            try {
                LlmExplanationResponse resp = objectMapper.readValue(cached.get().getResponseJson(), LlmExplanationResponse.class);
                applyExplanation(finding, resp);
                log.debug("Applied cached AI explanation for rule {}", finding.getRuleId());
                return;
            } catch (Exception e) {
                log.warn("Failed to deserialize cached explanation: {}", e.getMessage());
            }
        }

        // 2. Query NVIDIA Nemotron Ultra if active
        Optional<LlmExplanationResponse> aiResult = nemotronAiClient.explainFinding(
                finding.getRuleId(),
                finding.getCategory().name(),
                finding.getSeverity().name(),
                finding.getConfidence().name(),
                finding.getFilePath(),
                finding.getStartLine(),
                finding.getEndLine(),
                maskedEvidence,
                finding.getRemediation()
        );

        if (aiResult.isPresent()) {
            LlmExplanationResponse resp = aiResult.get();
            applyExplanation(finding, resp);

            // Save to Cache
            try {
                String json = objectMapper.writeValueAsString(resp);
                LlmCacheEntity cacheEntity = new LlmCacheEntity(
                        cacheKey, "NVIDIA_NEMOTRON", "nvidia/llama-3.1-nemotron-70b-instruct",
                        json, Instant.now(), Instant.now().plus(7, ChronoUnit.DAYS)
                );
                cacheRepository.save(cacheEntity);
            } catch (Exception e) {
                log.debug("Failed to cache explanation: {}", e.getMessage());
            }
            return;
        }

        // 3. Fallback to deterministic template
        LlmExplanationResponse fallback = templateService.generateFallback(finding);
        applyExplanation(finding, fallback);
    }

    public void enrichFindingDeterministic(FindingEntity finding) {
        String maskedEvidence = secretMasker.mask(finding.getEvidenceMasked());
        finding.setEvidenceMasked(maskedEvidence);
        LlmExplanationResponse fallback = templateService.generateFallback(finding);
        applyExplanation(finding, fallback);
    }

    private void applyExplanation(FindingEntity finding, LlmExplanationResponse resp) {
        if (resp.title() != null && !resp.title().isBlank()) finding.setTitle(resp.title());
        if (resp.explanation() != null && !resp.explanation().isBlank()) finding.setDescription(resp.explanation());
        if (resp.impact() != null && !resp.impact().isBlank()) finding.setImpact(resp.impact());
        if (resp.remediation() != null && !resp.remediation().isBlank()) finding.setRemediation(resp.remediation());
        if (resp.suggestedFix() != null && !resp.suggestedFix().isBlank()) finding.setSuggestedFix(resp.suggestedFix());
        if (resp.requiresManualReview() != null) finding.setRequiresManualReview(resp.requiresManualReview());
        if (resp.references() != null && !resp.references().isEmpty()) finding.setReferences(resp.references());
    }

    private String computeCacheKey(String ruleId, String maskedEvidence, String remediation) {
        try {
            String raw = ruleId + "|" + (maskedEvidence != null ? maskedEvidence.trim() : "") + "|" + (remediation != null ? remediation.trim() : "");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return (ruleId + "-" + maskedEvidence.hashCode());
        }
    }
}
