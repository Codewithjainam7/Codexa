package com.codexa.security.ast;

import com.github.javaparser.ast.Node;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AstSnippetExtractor {

    public String extractNodeSnippet(Node node, List<String> sourceLines) {
        if (node == null || sourceLines == null || sourceLines.isEmpty()) {
            return "";
        }

        if (node.getRange().isPresent()) {
            var range = node.getRange().get();
            int startLine = range.begin.line;
            int endLine = range.end.line;
            return extractLines(sourceLines, startLine, endLine, 0);
        }

        return node.toString();
    }

    public String extractLinesWithContext(List<String> sourceLines, int startLine, int endLine, int contextLines) {
        return extractLines(sourceLines, startLine, endLine, contextLines);
    }

    private String extractLines(List<String> sourceLines, int startLine, int endLine, int contextLines) {
        if (sourceLines == null || sourceLines.isEmpty() || startLine <= 0) {
            return "";
        }

        int totalLines = sourceLines.size();
        int from = Math.max(0, startLine - 1 - contextLines);
        int to = Math.min(totalLines, endLine + contextLines);

        if (from >= to) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            sb.append(sourceLines.get(i));
            if (i < to - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
