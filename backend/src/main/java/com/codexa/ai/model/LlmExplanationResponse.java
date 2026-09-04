package com.codexa.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmExplanationResponse(
        String title,
        String explanation,
        String impact,
        String remediation,
        String suggestedFix,
        @JsonProperty("assumptions")
        JsonNode assumptionsNode,
        List<String> references,
        Boolean requiresManualReview
) {
    public LlmExplanationResponse(
            String title,
            String explanation,
            String impact,
            String remediation,
            String suggestedFix,
            String assumptions,
            List<String> references,
            Boolean requiresManualReview
    ) {
        this(title, explanation, impact, remediation, suggestedFix,
                assumptions != null ? TextNode.valueOf(assumptions) : null,
                references, requiresManualReview);
    }

    public String assumptions() {
        if (assumptionsNode == null || assumptionsNode.isNull()) return "";
        if (assumptionsNode.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode item : assumptionsNode) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(item.asText());
            }
            return sb.toString();
        }
        return assumptionsNode.asText();
    }
}
