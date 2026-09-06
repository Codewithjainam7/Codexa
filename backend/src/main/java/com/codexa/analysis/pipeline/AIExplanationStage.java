package com.codexa.analysis.pipeline;

import com.codexa.ai.client.NvidiaNemotronAiClient;
import com.codexa.ai.service.AIExplanationService;
import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.persistence.entity.AnalysisJobEntity;
import com.codexa.persistence.entity.FindingEntity;
import com.codexa.persistence.repository.AnalysisJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@Order(30)
public class AIExplanationStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(AIExplanationStage.class);

    private final AIExplanationService aiExplanationService;
    private final NvidiaNemotronAiClient aiClient;
    private final AnalysisJobRepository jobRepository;

    public AIExplanationStage(
            AIExplanationService aiExplanationService,
            NvidiaNemotronAiClient aiClient,
            AnalysisJobRepository jobRepository
    ) {
        this.aiExplanationService = aiExplanationService;
        this.aiClient = aiClient;
        this.jobRepository = jobRepository;
    }

    @Override
    public String getStageName() {
        return "AI_EXPLANATION_AND_REMEDIATION";
    }

    @Override
    public void execute(PipelineContext context) {
        log.info("Executing AI Explanation & Multi-Language Review stage for jobId={}", context.getJobId());

        // 1. Enrich existing deterministic findings: top-3 with LLM, instant template for remainder
        if (context.getFindings() != null && !context.getFindings().isEmpty()) {
            List<FindingEntity> allFindings = context.getFindings();
            // Sort by severity so CRITICAL and HIGH severity findings get LLM priority
            allFindings.sort((a, b) -> {
                int sevA = a.getSeverity() == Severity.CRITICAL ? 4 : a.getSeverity() == Severity.HIGH ? 3 : a.getSeverity() == Severity.MEDIUM ? 2 : 1;
                int sevB = b.getSeverity() == Severity.CRITICAL ? 4 : b.getSeverity() == Severity.HIGH ? 3 : b.getSeverity() == Severity.MEDIUM ? 2 : 1;
                return Integer.compare(sevB, sevA);
            });

            for (int i = 0; i < allFindings.size(); i++) {
                FindingEntity finding = allFindings.get(i);
                try {
                    if (i < 3) {
                        aiExplanationService.enrichFinding(finding);
                    } else {
                        aiExplanationService.enrichFindingDeterministic(finding);
                    }
                } catch (Exception e) {
                    log.warn("Failed to enrich finding {} with AI explanation: {}", finding.getRuleId(), e.getMessage());
                    try {
                        aiExplanationService.enrichFindingDeterministic(finding);
                    } catch (Exception ignored) {}
                }
            }
        }

        // 2. Perform Multi-Language Holistic AI Review (TypeScript, JS, Python, Fullstack)
        boolean configured = aiClient.isConfigured();
        log.info("AI review check for jobId={}: aiConfigured={}, stagingDirExists={}",
                context.getJobId(), configured, context.getStagingDirectory() != null && Files.exists(context.getStagingDirectory()));

        if (configured && context.getStagingDirectory() != null && Files.exists(context.getStagingDirectory())) {
            try {
                List<NvidiaNemotronAiClient.CodeSnippet> snippets = collectSourceSnippets(context.getStagingDirectory());
                log.info("Collected {} source snippets for AI multi-language review.", snippets.size());
                if (!snippets.isEmpty()) {
                    List<NvidiaNemotronAiClient.AiReviewFinding> aiFindings = aiClient.reviewMultiLanguageCodebase(snippets);
                    log.info("Received {} AI findings from LLM model.", aiFindings.size());
                    
                    AnalysisJobEntity job = jobRepository.findById(context.getJobId()).orElse(null);
                    if (job != null) {
                        for (NvidiaNemotronAiClient.AiReviewFinding f : aiFindings) {
                            FindingEntity entity = new FindingEntity();
                            entity.setId(UUID.randomUUID());
                            entity.setJob(job);
                            entity.setRuleId(f.ruleId());
                            entity.setCategory(Category.valueOf(f.category().toUpperCase()));
                            entity.setSeverity(Severity.valueOf(f.severity().toUpperCase()));
                            entity.setConfidence(Confidence.valueOf(f.confidence().toUpperCase()));
                            entity.setTitle(f.title());
                            entity.setDescription(f.description());
                            entity.setImpact(f.impact());
                            entity.setRemediation(f.remediation());
                            entity.setOwaspMapping(f.owaspMapping());
                            entity.setFilePath(f.filePath());
                            entity.setStartLine(f.startLine());
                            entity.setEndLine(f.endLine());
                            entity.setEvidenceMasked(f.evidenceMasked());
                            entity.setSuggestedFix(f.suggestedFix());
                            entity.setPriorityScore(f.priorityScore());
                            entity.setRequiresManualReview(false);
                            entity.setReferences(List.of("https://owasp.org/Top10/"));
                            context.getFindings().add(entity);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Multi-language AI review stage error: {}", e.getMessage(), e);
            }
        }

        log.info("AI explanations and multi-language review completed for {} findings.", context.getFindings().size());
    }

    private List<NvidiaNemotronAiClient.CodeSnippet> collectSourceSnippets(Path stagingDir) {
        List<NvidiaNemotronAiClient.CodeSnippet> list = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(stagingDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(this::isRelevantSourceFile)
                    .limit(3)
                    .forEach(file -> {
                        try {
                            String rel = stagingDir.relativize(file).toString().replace("\\", "/");
                            String content = Files.readString(file, StandardCharsets.UTF_8);
                            list.add(new NvidiaNemotronAiClient.CodeSnippet(rel, content));
                        } catch (Exception ignored) {}
                    });
        } catch (Exception e) {
            log.debug("Error collecting snippets: {}", e.getMessage());
        }
        return list;
    }

    private boolean isRelevantSourceFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return name.endsWith(".ts") || name.endsWith(".tsx") || name.endsWith(".js") || name.endsWith(".jsx") ||
                name.endsWith(".py") || name.endsWith(".java") || name.endsWith(".go") || name.endsWith(".php") ||
                name.endsWith(".env") || name.endsWith(".json");
    }
}
