package com.codexa.rules.quality;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import com.codexa.rules.api.AnalysisRule;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import com.codexa.security.ast.ParsedJavaFile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CodeDuplicationRule implements AnalysisRule {

    private static final int SHINGLE_SIZE = 8;

    @Override
    public String getRuleId() {
        return "CR-QUAL-004";
    }

    @Override
    public String getName() {
        return "Duplicated Code Blocks";
    }

    @Override
    public Category getCategory() {
        return Category.QUALITY;
    }

    @Override
    public Severity getSeverity() {
        return Severity.LOW;
    }

    @Override
    public Confidence getDefaultConfidence() {
        return Confidence.MEDIUM;
    }

    @Override
    public String getOwaspMapping() {
        return "Code Maintainability";
    }

    @Override
    public List<RuleFinding> evaluate(RuleContext context) {
        List<RuleFinding> findings = new ArrayList<>();
        ParsedJavaFile parsedFile = context.getParsedJavaFile();

        if (parsedFile == null || parsedFile.getLines() == null) {
            return findings;
        }

        List<String> rawLines = parsedFile.getLines();
        List<String> normalizedLines = new ArrayList<>();
        List<Integer> originalLineNumbers = new ArrayList<>();

        for (int i = 0; i < rawLines.size(); i++) {
            String norm = rawLines.get(i).trim();
            // Skip empty lines, single braces, imports, package statements
            if (!norm.isEmpty() && !norm.equals("{") && !norm.equals("}") && !norm.startsWith("import ") && !norm.startsWith("package ")) {
                normalizedLines.add(norm);
                originalLineNumbers.add(i + 1);
            }
        }

        if (normalizedLines.size() < SHINGLE_SIZE * 2) {
            return findings;
        }

        Map<String, Integer> shingleMap = new HashMap<>();
        for (int i = 0; i <= normalizedLines.size() - SHINGLE_SIZE; i++) {
            String shingle = String.join("\n", normalizedLines.subList(i, i + SHINGLE_SIZE));
            if (shingleMap.containsKey(shingle)) {
                int previousIndex = shingleMap.get(shingle);
                // Ensure non-overlapping
                if (i >= previousIndex + SHINGLE_SIZE) {
                    int startLine = originalLineNumbers.get(i);
                    int endLine = originalLineNumbers.get(i + SHINGLE_SIZE - 1);
                    int prevStartLine = originalLineNumbers.get(previousIndex);

                    findings.add(RuleFinding.builder()
                            .ruleId(getRuleId())
                            .category(getCategory())
                            .severity(getSeverity())
                            .confidence(getDefaultConfidence())
                            .title("Duplicated code block (" + SHINGLE_SIZE + "+ lines matching line " + prevStartLine + ")")
                            .description("Detected identical logic sequence of " + SHINGLE_SIZE + "+ lines previously appearing at line " + prevStartLine + ". Duplicated code leads to maintenance overhead and divergent bug fixes.")
                            .impact("Higher defect probability when business logic is modified in one location but forgotten in another.")
                            .remediation("Extract the common logic into a shared helper method or utility class.")
                            .suggestedFix("// Extract shared sequence into a reusable private method.")
                            .owaspMapping(getOwaspMapping())
                            .filePath(parsedFile.getRelativePath())
                            .startLine(startLine)
                            .endLine(endLine)
                            .evidence(shingle)
                            .references(List.of("https://refactoring.guru/smells/duplicate-code"))
                            .build());
                    break; // Limit to 1 duplicate report per file to avoid noise
                }
            } else {
                shingleMap.put(shingle, i);
            }
        }

        return findings;
    }
}
