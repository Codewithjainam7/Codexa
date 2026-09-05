package com.codexa.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.ArrayList;
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
        @JsonProperty("references")
        JsonNode referencesNode,
        Boolean requiresManualReview
) {
    public LlmExplanationResponse(
            String title,
            String explanation,
            String impact,
            String remediation,
            String suggestedFix,
            String assumptions,
            List<String> referencesList,
            Boolean requiresManualReview
    ) {
        this(title, explanation, impact, remediation, suggestedFix,
                assumptions != null ? TextNode.valueOf(assumptions) : null,
                referencesList != null ? TextNode.valueOf(String.join(", ", referencesList)) : null,
                requiresManualReview);
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
        return assumptionsNode.asText("");
    }

    public List<String> references() {
        List<String> list = new ArrayList<>();
        if (referencesNode == null || referencesNode.isNull()) {
            list.add("https://owasp.org/Top10/");
            return list;
        }
        if (referencesNode.isArray()) {
            for (JsonNode item : referencesNode) {
                String text = item.asText("").trim();
                if (!text.isEmpty()) {
                    list.add(text);
                }
            }
        } else {
            String text = referencesNode.asText("").trim();
            if (!text.isEmpty()) {
                list.add(text);
            }
        }
        if (list.isEmpty()) {
            list.add("https://owasp.org/Top10/");
        }
        return list;
    }
}
