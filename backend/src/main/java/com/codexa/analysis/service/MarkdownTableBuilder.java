package com.codexa.analysis.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for generating GitHub-flavored Markdown tables.
 */
public class MarkdownTableBuilder {

    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();

    public MarkdownTableBuilder headers(String... colHeaders) {
        headers.addAll(List.of(colHeaders));
        return this;
    }

    public MarkdownTableBuilder addRow(String... cells) {
        rows.add(List.of(cells));
        return this;
    }

    public String build() {
        if (headers.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
        sb.append("|");
        for (int i = 0; i < headers.size(); i++) {
            sb.append(" --- |");
        }
        sb.append("\n");

        for (List<String> row : rows) {
            sb.append("| ").append(String.join(" | ", row)).append(" |\n");
        }
        return sb.toString();
    }
}
