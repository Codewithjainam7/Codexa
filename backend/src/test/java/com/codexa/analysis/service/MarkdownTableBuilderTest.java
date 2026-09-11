package com.codexa.analysis.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MarkdownTableBuilderTest {

    @Test
    void shouldBuildMarkdownTableCorrectly() {
        String md = new MarkdownTableBuilder()
                .headers("Rule ID", "Severity", "Count")
                .addRow("CR-SEC-001", "CRITICAL", "3")
                .addRow("CR-QUAL-002", "MEDIUM", "1")
                .build();

        assertThat(md).contains("| Rule ID | Severity | Count |");
        assertThat(md).contains("| --- | --- | --- |");
        assertThat(md).contains("| CR-SEC-001 | CRITICAL | 3 |");
    }
}
