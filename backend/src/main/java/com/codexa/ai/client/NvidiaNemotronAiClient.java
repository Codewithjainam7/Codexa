package com.codexa.ai.client;

import com.codexa.ai.model.LlmExplanationResponse;
import com.codexa.config.CodexaProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
    private static final String DEFAULT_MODEL = "meta-llama/llama-3.3-70b-instruct:free";
    private static final List<String> FALLBACK_MODELS = List.of(
            "meta-llama/llama-3.3-70b-instruct:free",
            "mistralai/mistral-small-24b-instruct-2501:free"
    );

    private final CodexaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public NvidiaNemotronAiClient(CodexaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    public boolean isConfigured() {
        String apiKey = getApiKey();
        boolean hasKey = apiKey != null && !apiKey.isBlank() && !apiKey.contains("placeholder");
        boolean isEnabled = (properties.ai() != null && properties.ai().enabled()) || hasKey;
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
            if (safeContent.length() > 800) {
                safeContent = safeContent.substring(0, 800) + "\n// ... [truncated]";
            }
            codeContext.append("\n--- FILE: ").append(snippet.filePath()).append(" ---\n")
                    .append(safeContent).append("\n");
        }

        String prompt = """
                You are a principal application security engineer.
                Analyze the following codebase for security vulnerabilities (SQLi, XSS, RCE, Secrets, Broken Access Control) and production readiness defects.
                
                Codebase Files:
                %s
                
                Return a valid JSON object ONLY with the following structure:
                {
                  "findings": [
                    {
                      "ruleId": "CR-SEC-001",
                      "category": "SECURITY",
                      "severity": "HIGH",
                      "confidence": "HIGH",
                      "title": "Short title",
                      "description": "Clear explanation",
                      "impact": "Security impact",
                      "remediation": "How to fix",
                      "owaspMapping": "A03:2021-Injection",
                      "filePath": "path/to/file",
                      "startLine": 1,
                      "endLine": 1,
                      "evidenceMasked": "code line",
                      "suggestedFix": "fixed code",
                      "priorityScore": 0.85
                    }
                  ]
                }
                """.formatted(codeContext.toString());

        String apiKey = getApiKey();
        String endpoint = determineEndpoint(apiKey);
        List<String> modelsToTry = new ArrayList<>(FALLBACK_MODELS);

        for (String model : modelsToTry) {
            try {
                Map<String, Object> requestBody = Map.of(
                        "model", model,
                        "temperature", 0.1,
                        "max_tokens", 1024,
                        "messages", List.of(
                                Map.of("role", "system", "content", "You are an expert SAST security engineer. Return JSON only."),
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
                                        f.path("title").asText("Security Finding"),
                                        f.path("description").asText(""),
                                        f.path("impact").asText("Potential security vulnerability."),
                                        f.path("remediation").asText("Apply secure coding best practices."),
                                        f.path("owaspMapping").asText("A05:2021-Security Misconfiguration"),
                                        f.path("filePath").asText(snippets.get(0).filePath()),
                                        f.path("startLine").asInt(1),
                                        f.path("endLine").asInt(1),
                                        f.path("evidenceMasked").asText(""),
                                        f.path("suggestedFix").asText(""),
                                        f.path("priorityScore").asDouble(0.7)
                                ));
                            }
                            log.info("AI Code Review identified {} findings with model {}", list.size(), model);
                            return list;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Model {} failed ({})", model, e.getMessage());
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
        List<String> modelsToTry = List.of(DEFAULT_MODEL, "meta-llama/llama-3.3-70b-instruct:free");

        for (String model : modelsToTry) {
            try {
                Map<String, Object> requestBody = Map.of(
                        "model", model,
                        "temperature", 0.1,
                        "max_tokens", 800,
                        "messages", List.of(
                                Map.of("role", "system", "content", "You are an application security reviewer. Return JSON only: {title, explanation, impact, remediation, suggestedFix, assumptions, references}."),
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
                break; // Fallback directly to fast deterministic template on first fail
            }
        }

        return Optional.empty();
    }

    private HttpResponse<String> sendChatRequest(String endpoint, String apiKey, Map<String, Object> requestBody) throws Exception {
        String requestJson = objectMapper.writeValueAsString(requestBody);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(4))
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
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        content = content.trim();

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
                Explain this security finding as JSON: {title, explanation, impact, remediation, suggestedFix, assumptions, references}.
                Rule: %s
                Category: %s
                Severity: %s
                Confidence: %s
                File: %s
                Lines: %d-%d
                Evidence: %s
                Remediation: %s
                """.formatted(ruleId, category, severity, confidence, filePath, startLine, endLine, evidence, remediation);
    }
}
