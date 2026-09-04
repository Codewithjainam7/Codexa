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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class NvidiaNemotronAiClient {

    private static final Logger log = LoggerFactory.getLogger(NvidiaNemotronAiClient.class);
    private static final String DEFAULT_NVIDIA_ENDPOINT = "https://integrate.api.nvidia.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "nvidia/llama-3.1-nemotron-70b-instruct";

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
        return apiKey != null && !apiKey.isBlank() && !apiKey.contains("placeholder");
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

        try {
            String apiKey = getApiKey();
            String model = properties.ai().model() != null && !properties.ai().model().isBlank()
                    ? properties.ai().model()
                    : DEFAULT_MODEL;

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "max_tokens", 1024,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are an application-security reviewer. Explain only the supplied finding. Return JSON only: {title, explanation, impact, remediation, suggestedFix, assumptions, references}. Do not invent CVEs, line numbers, dependencies, or vulnerabilities. If context is insufficient, say requires_manual_review=true."),
                            Map.of("role", "user", "content", prompt)
                    )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DEFAULT_NVIDIA_ENDPOINT))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .timeout(Duration.ofMillis(properties.ai().timeoutMs()))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("NVIDIA Nemotron API returned status {}: {}", response.statusCode(), response.body());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode()) {
                return Optional.empty();
            }

            String content = contentNode.asText().trim();
            // Sanitize markdown fences if LLM wrapped in ```json ... ```
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }

            LlmExplanationResponse explanation = objectMapper.readValue(content.trim(), LlmExplanationResponse.class);
            return Optional.of(explanation);

        } catch (Exception e) {
            log.warn("Failed to generate NVIDIA Nemotron AI explanation for {}: {}", ruleId, e.getMessage());
            return Optional.empty();
        }
    }

    private String getApiKey() {
        String envKey = System.getenv("NVIDIA_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        return properties.ai().apiKey();
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
