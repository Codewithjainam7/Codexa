package com.codexa.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmExplanationResponse(
        String title,
        String explanation,
        String impact,
        String remediation,
        String suggestedFix,
        String assumptions,
        List<String> references,
        Boolean requiresManualReview
) {
}
