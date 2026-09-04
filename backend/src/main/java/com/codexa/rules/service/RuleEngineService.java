package com.codexa.rules.service;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.persistence.entity.FindingEntity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.ParsedJavaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class RuleEngineService {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineService.class);

    private final List<AnalysisRule> rules;

    public RuleEngineService(List<AnalysisRule> rules) {
        this.rules = rules != null ? rules : List.of();
        log.info("Initialized Codexa Rule Engine with {} deterministic rules.", this.rules.size());
    }

    public List<FindingEntity> analyze(PipelineContext pipelineContext) {
        List<FindingEntity> findings = new ArrayList<>();
        Set<String> seenHashes = new HashSet<>();

        List<ParsedJavaFile> parsedFiles = pipelineContext.getParsedJavaFiles();

        for (ParsedJavaFile parsedFile : parsedFiles) {
            RuleContext ruleContext = new RuleContext(parsedFile, pipelineContext);

            for (AnalysisRule rule : rules) {
                // If it's a universal rule, skip per-file iteration and let it run in repoContext
                if (rule instanceof com.codexa.rules.universal.UniversalMultiLanguageRule) {
                    continue;
                }
                try {
                    List<RuleFinding> ruleFindings = rule.evaluate(ruleContext);
                    for (RuleFinding rf : ruleFindings) {
                        String hash = computeDeduplicationHash(rf.ruleId(), rf.filePath(), rf.startLine(), rf.endLine(), rf.evidence());
                        if (!seenHashes.contains(hash)) {
                            seenHashes.add(hash);
                            findings.add(toEntity(rf, hash));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Rule '{}' threw exception on file '{}': {}", rule.getRuleId(), parsedFile.getRelativePath(), e.getMessage());
                }
            }
        }

        // Evaluate repository-wide rules (multi-language scanner, configs, repo-level audits)
        RuleContext repoContext = new RuleContext(null, pipelineContext);
        for (AnalysisRule rule : rules) {
            try {
                if (rule instanceof com.codexa.rules.universal.UniversalMultiLanguageRule || parsedFiles.isEmpty()) {
                    List<RuleFinding> ruleFindings = rule.evaluate(repoContext);
                    for (RuleFinding rf : ruleFindings) {
                        String hash = computeDeduplicationHash(rf.ruleId(), rf.filePath(), rf.startLine(), rf.endLine(), rf.evidence());
                        if (!seenHashes.contains(hash)) {
                            seenHashes.add(hash);
                            findings.add(toEntity(rf, hash));
                        }
                    }
                }
            } catch (Exception ignored) {
                // Skip rules that require Java AST
            }
        }

        log.info("Analysis of jobId={} generated {} unique deterministic findings across {} rules.",
                pipelineContext.getJobId(), findings.size(), rules.size());

        return findings;
    }

    private FindingEntity toEntity(RuleFinding rf, String deduplicationHash) {
        FindingEntity entity = new FindingEntity();
        entity.setId(UUID.randomUUID());
        entity.setRuleId(rf.ruleId());
        entity.setCategory(rf.category());
        entity.setSeverity(rf.severity());
        entity.setConfidence(rf.confidence());
        entity.setTitle(rf.title());
        entity.setDescription(rf.description());
        entity.setImpact(rf.impact());
        entity.setRemediation(rf.remediation());
        entity.setOwaspMapping(rf.owaspMapping());
        entity.setFilePath(rf.filePath());
        entity.setStartLine(rf.startLine());
        entity.setEndLine(rf.endLine());
        entity.setEvidenceMasked(rf.evidence());
        entity.setSuggestedFix(rf.suggestedFix());
        entity.setRequiresManualReview(rf.requiresManualReview());
        entity.setReferences(rf.references());
        entity.setDeduplicationHash(deduplicationHash);
        return entity;
    }

    private String computeDeduplicationHash(String ruleId, String filePath, int startLine, int endLine, String evidence) {
        try {
            String raw = ruleId + "|" + filePath + "|" + startLine + "-" + endLine + "|" + (evidence != null ? evidence.trim() : "");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
