package com.codexa.rules.api;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;

import java.util.List;

public record RuleFinding(
        String ruleId,
        Category category,
        Severity severity,
        Confidence confidence,
        String title,
        String description,
        String impact,
        String remediation,
        String owaspMapping,
        String filePath,
        int startLine,
        int endLine,
        String evidence,
        String suggestedFix,
        boolean requiresManualReview,
        List<String> references
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ruleId;
        private Category category;
        private Severity severity;
        private Confidence confidence = Confidence.HIGH;
        private String title;
        private String description;
        private String impact;
        private String remediation;
        private String owaspMapping;
        private String filePath;
        private int startLine;
        private int endLine;
        private String evidence;
        private String suggestedFix;
        private boolean requiresManualReview = false;
        private List<String> references = List.of();

        public Builder ruleId(String ruleId) { this.ruleId = ruleId; return this; }
        public Builder category(Category category) { this.category = category; return this; }
        public Builder severity(Severity severity) { this.severity = severity; return this; }
        public Builder confidence(Confidence confidence) { this.confidence = confidence; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder impact(String impact) { this.impact = impact; return this; }
        public Builder remediation(String remediation) { this.remediation = remediation; return this; }
        public Builder owaspMapping(String owaspMapping) { this.owaspMapping = owaspMapping; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder startLine(int startLine) { this.startLine = startLine; return this; }
        public Builder endLine(int endLine) { this.endLine = endLine; return this; }
        public Builder evidence(String evidence) { this.evidence = evidence; return this; }
        public Builder suggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; return this; }
        public Builder requiresManualReview(boolean requiresManualReview) { this.requiresManualReview = requiresManualReview; return this; }
        public Builder references(List<String> references) { this.references = references; return this; }

        public RuleFinding build() {
            return new RuleFinding(
                    ruleId, category, severity, confidence, title, description, impact, remediation,
                    owaspMapping, filePath, startLine, endLine, evidence, suggestedFix, requiresManualReview, references
            );
        }
    }
}
