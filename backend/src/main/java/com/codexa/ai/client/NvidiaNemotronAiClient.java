package com.codexa.ai.client;

import com.codexa.ai.model.LlmExplanationResponse;
import com.codexa.config.CodexaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class NvidiaNemotronAiClient {

    private static final Logger log = LoggerFactory.getLogger(NvidiaNemotronAiClient.class);
    private static final String DEFAULT_OPENROUTER_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_NVIDIA_ENDPOINT = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "nvidia/nemotron-3-ultra-550b-a55b:free";
    private static final List<String> FALLBACK_MODELS = List.of(
            "nvidia/nemotron-3-ultra-550b-a55b:free",
            "nvidia/nemotron-3.5-lightning:free",
            "thinkingmachines/inkling-small:free",
            "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free",
            "nvidia/nemotron-3-super-120b-a12b:free"
    );

    private final CodexaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public NvidiaNemotronAiClient(CodexaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public boolean isConfigured() {
        String apiKey = getApiKey();
        boolean hasKey = apiKey != null && !apiKey.isBlank() && !apiKey.contains("placeholder");
        boolean isEnabled = (properties.ai() != null && properties.ai().enabled()) || hasKey;
        log.info("Codexa AI configuration check: enabled={}, hasKey={}", isEnabled, hasKey);
        return isEnabled && hasKey;
    }

    public record CodeSnippet(String filePath, String content) {}

    public record AiReviewFinding(
            String ruleId,
            String category,
            String severity,
            String confidence,
            String title,
            String description,
            String impact,
            String remediation,
            String owaspMapping,
            String filePath,
            int startLine,
            int endLine,
            String evidenceMasked,
            String suggestedFix,
            double priorityScore
    ) {}

    public List<AiReviewFinding> reviewMultiLanguageCodebase(List<CodeSnippet> snippets) {
        if (!isConfigured() || snippets == null || snippets.isEmpty()) {
            return List.of();
        }

        StringBuilder codeContext = new StringBuilder();
        int count = 0;
        for (CodeSnippet snippet : snippets) {
            if (count++ >= 4) break;
            String safeContent = snippet.content();
            if (safeContent.length() > 1000) {
                safeContent = safeContent.substring(0, 1000) + "\n// ... [truncated for review context]";
            }
            codeContext.append("\n--- FILE: ").append(snippet.filePath()).append(" ---\n")
                    .append(safeContent).append("\n");
        }

        String prompt = """
                You are a principal application security engineer and code reviewer.
                Analyze the following multi-language codebase (TypeScript, JavaScript, Python, Java, etc.) for security vulnerabilities, OWASP Top 10 risks, secret exposure, broken access control, XSS, insecure configs, missing error handling, and production readiness defects.
                
                Codebase Files:
                %s
                
                Return JSON only in this exact format:
                {
                  "findings": [
                    {
                      "ruleId": "CR-SEC-001 or CR-XSS-001 or CR-AUTH-001 or CR-QUAL-001 etc",
                      "category": "SECURITY or QUALITY or OPERATIONS",
                      "severity": "CRITICAL or HIGH or MEDIUM or LOW",
                      "confidence": "HIGH or MEDIUM",
                      "title": "Clear issue title",
                      "description": "Plain-English explanation of why this is dangerous or bad practice",
                      "impact": "Concrete impact on production systems or users",
                      "remediation": "How to fix it properly",
                      "owaspMapping": "OWASP category e.g. A03:2021-Injection",
                      "filePath": "exact relative path of the file",
                      "startLine": 10,
                      "endLine": 15,
                      "evidenceMasked": "exact vulnerable snippet",
                      "suggestedFix": "fixed code snippet",
                      "priorityScore": 0.8
                    }
                  ]
                }
                
                Do not invent files that do not exist in the prompt. Return valid JSON only.
                """.formatted(codeContext.toString());

        String apiKey = getApiKey();
        String endpoint = determineEndpoint(apiKey);
        List<String> modelsToTry = new ArrayList<>();
        String primaryModel = properties.ai().model() != null && !properties.ai().model().isBlank()
                ? properties.ai().model()
                : DEFAULT_MODEL;
        modelsToTry.add(primaryModel);
        modelsToTry.addAll(FALLBACK_MODELS);

        for (String model : modelsToTry) {
            try {
                log.info("Executing AI Code Review using model: {}", model);
                Map<String, Object> requestBody = Map.of(
                        "model", model,
                        "temperature", 0.1,
                        "max_tokens", 2500,
                        "messages", List.of(
                                Map.of("role", "system", "content", "You are an expert static security auditor. Return JSON only with a 'findings' array."),
                                Map.of("role", "user", "content", prompt)
                        )
                );

                HttpResponse<String> response = sendChatRequest(endpoint, apiKey, requestBody);
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
                    if (!contentNode.isMissingNode()) {
                        String cleanJson = cleanJsonString(contentNode.asText().trim());
                        JsonNode jsonRoot = objectMapper.readTree(cleanJson);
                        JsonNode findingsArray = jsonRoot.path("findings");
                        if (findingsArray.isArray()) {
                            List<AiReviewFinding> list = new ArrayList<>();
                            for (JsonNode f : findingsArray) {
                                list.add(new AiReviewFinding(
                                        f.path("ruleId").asText("CR-AI-001"),
                                        f.path("category").asText("SECURITY"),
                                        f.path("severity").asText("MEDIUM"),
                                        f.path("confidence").asText("HIGH"),
                                        f.path("title").asText("Security or Code Quality Finding"),
                                        f.path("description").asText(""),
                                        f.path("impact").asText("Potential security vulnerability or code defect."),
                                        f.path("remediation").asText("Apply the suggested remediation."),
                                        f.path("owaspMapping").asText("A05:2021-Security Misconfiguration"),
                                        f.path("filePath").asText(snippets.get(0).filePath()),
                                        f.path("startLine").asInt(1),
                                        f.path("endLine").asInt(1),
                                        f.path("evidenceMasked").asText(""),
                                        f.path("suggestedFix").asText(""),
                                        f.path("priorityScore").asDouble(0.7)
                                ));
                            }
                            log.info("AI Code Review identified {} multi-language findings with model {}", list.size(), model);
                            return list;
                        }
                    }
                } else {
                    log.warn("Model {} returned HTTP {}", model, response.statusCode());
                }
            } catch (Exception e) {
                log.warn("Model {} failed ({}), trying next fallback...", model, e.getMessage());
            }
        }

        return List.of();
    }

    public Optional<LlmExplanationResponse> explainFinding(
            String ruleId,
            String category,
            String severity,
            String confidence,
            String filePath,
            int startLine,
            int endLine,
            String maskedEvidence,
            String knownRemediation
    ) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        String prompt = buildPrompt(ruleId, category, severity, confidence, filePath, startLine, endLine, maskedEvidence, knownRemediation);

        String apiKey = getApiKey();
        String endpoint = determineEndpoint(apiKey);
        List<String> modelsToTry = new ArrayList<>();
        String primaryModel = properties.ai().model() != null && !properties.ai().model().isBlank()
                ? properties.ai().model()
                : DEFAULT_MODEL;
        modelsToTry.add(primaryModel);
        modelsToTry.addAll(FALLBACK_MODELS);

        for (String model : modelsToTry) {
            try {
                Map<String, Object> requestBody = Map.of(
                        "model", model,
                        "temperature", 0.2,
                        "max_tokens", 1024,
                        "messages", List.of(
                                Map.of("role", "system", "content", "You are an application-security reviewer. Explain only the supplied finding. Return JSON only: {title, explanation, impact, remediation, suggestedFix, assumptions, references}. Do not invent CVEs, line numbers, dependencies, or vulnerabilities. If context is insufficient, say requires_manual_review=true."),
                                Map.of("role", "user", "content", prompt)
                        )
                );

                HttpResponse<String> response = sendChatRequest(endpoint, apiKey, requestBody);
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
                    if (!contentNode.isMissingNode()) {
                        String content = cleanJsonString(contentNode.asText().trim());
                        LlmExplanationResponse explanation = objectMapper.readValue(content, LlmExplanationResponse.class);
                        return Optional.of(explanation);
                    }
                }
            } catch (Exception e) {
                log.debug("Explain finding model {} failed: {}", model, e.getMessage());
            }
        }

        return Optional.empty();
    }

    private HttpResponse<String> sendChatRequest(String endpoint, String apiKey, Map<String, Object> requestBody) throws Exception {
        String requestJson = objectMapper.writeValueAsString(requestBody);
        int timeoutMs = properties.ai() != null && properties.ai().timeoutMs() > 0 ? Math.min(properties.ai().timeoutMs(), 20000) : 20000;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .timeout(Duration.ofMillis(timeoutMs))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson));

        if (endpoint.contains("openrouter.ai")) {
            builder.header("HTTP-Referer", "https://github.com/Codewithjainam7/Codexa");
            builder.header("X-Title", "Codexa Security Platform");
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String cleanJsonString(String input) {
        if (input == null) return "{}";
        String content = input.trim();
        int firstBrace = content.indexOf('{');
        int lastBrace = content.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return content.substring(firstBrace, lastBrace + 1).trim();
        }
        int firstBracket = content.indexOf('[');
        int lastBracket = content.lastIndexOf(']');
        if (firstBracket != -1 && lastBracket != -1 && lastBracket > firstBracket) {
            return content.substring(firstBracket, lastBracket + 1).trim();
        }
        return content;
    }

    private String determineEndpoint(String apiKey) {
        if (properties.ai().endpoint() != null && !properties.ai().endpoint().isBlank()) {
            return properties.ai().endpoint();
        }
        if (apiKey != null && apiKey.startsWith("sk-or-")) {
            return DEFAULT_OPENROUTER_ENDPOINT;
        }
        return DEFAULT_NVIDIA_ENDPOINT;
    }

    private String getApiKey() {
        String envOpenRouter = System.getenv("OPENROUTER_API_KEY");
        if (envOpenRouter != null && !envOpenRouter.isBlank()) {
            return envOpenRouter.trim();
        }
        String envKey = System.getenv("NVIDIA_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        if (properties.ai() != null && properties.ai().apiKey() != null && !properties.ai().apiKey().isBlank()) {
            return properties.ai().apiKey().trim();
        }
        return "";
    }

    private String buildPrompt(String ruleId, String category, String severity, String confidence,
                               String filePath, int startLine, int endLine, String evidence, String remediation) {
        return """
                You are an application-security reviewer. Explain only the supplied finding.
                Rule: %s
                Category: %s
                Severity: %s
                Confidence: %s
                File: %s
                Lines: %d-%d
                Masked evidence: %s
                Known remediation: %s
                Return JSON only: {title, explanation, impact, remediation, suggestedFix, assumptions, references}.
                Do not invent CVEs, line numbers, dependencies, or vulnerabilities. If context is insufficient, say requires_manual_review=true.
                """.formatted(ruleId, category, severity, confidence, filePath, startLine, endLine, evidence, remediation);
    }
}
