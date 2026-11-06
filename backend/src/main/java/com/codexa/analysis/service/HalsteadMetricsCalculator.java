package com.codexa.analysis.service;

import com.codexa.analysis.model.HalsteadMetrics;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to calculate Halstead Software Science metrics and Maintainability Index 2.0.
 */
@Service
public class HalsteadMetricsCalculator {

    private static final Set<String> OPERATORS = Set.of(
            "+", "-", "*", "/", "%", "=", "+=", "-=", "*=", "/=",
            "==", "!=", "<", ">", "<=", ">=", "&&", "||", "!", "&", "|", "^", "~",
            "<<", ">>", ">>>", "?", ":", "->", "::", ".", ",", ";", "(", ")", "[", "]", "{", "}"
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "([a-zA-Z_][a-zA-Z0-9_]*)|([0-9]+(?:\\.[0-9]+)?)|(\"(?:\\\\\"|[^\"])*\")|('(?:\\\\'|[^'])*')|([+\\-*/%=!<>|&^~?:.,;()\\[\\]{}]+)"
    );

    public HalsteadMetrics calculate(String sourceCode, int cyclomaticComplexity, int loc) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return HalsteadMetrics.builder()
                    .maintainabilityIndex(100.0)
                    .build();
        }

        Set<String> uniqueOperators = new HashSet<>();
        Set<String> uniqueOperands = new HashSet<>();
        int totalOperators = 0;
        int totalOperands = 0;

        Matcher matcher = TOKEN_PATTERN.matcher(sourceCode);
        while (matcher.find()) {
            String token = matcher.group();
            if (OPERATORS.contains(token)) {
                uniqueOperators.add(token);
                totalOperators++;
            } else {
                uniqueOperands.add(token);
                totalOperands++;
            }
        }

        int eta1 = Math.max(1, uniqueOperators.size());
        int eta2 = Math.max(1, uniqueOperands.size());
        int N1 = totalOperators;
        int N2 = totalOperands;

        int vocabulary = eta1 + eta2;
        int length = N1 + N2;
        double volume = length * (Math.log(vocabulary) / Math.log(2));

        double difficulty = ((double) eta1 / 2.0) * ((double) N2 / (double) eta2);
        double effort = difficulty * volume;
        double timeSec = effort / 18.0;

        // Calibrated SEI Maintainability Index formula normalized to 0-100:
        // MI = 171 - 5.2 * ln(V) - 0.23 * CC - 16.2 * ln(LOC)
        double safeVol = Math.max(1.0, volume);
        double safeLoc = Math.max(1.0, (double) loc);
        double rawMI = 171.0 - (5.2 * Math.log(safeVol)) - (0.23 * cyclomaticComplexity) - (16.2 * Math.log(safeLoc));
        double normalizedMI = Math.max(0.0, Math.min(100.0, (rawMI / 171.0) * 100.0));

        return HalsteadMetrics.builder()
                .distinctOperators(eta1)
                .distinctOperands(eta2)
                .totalOperators(N1)
                .totalOperands(N2)
                .programVocabulary(vocabulary)
                .programLength(length)
                .volume(Math.round(volume * 100.0) / 100.0)
                .difficulty(Math.round(difficulty * 100.0) / 100.0)
                .effort(Math.round(effort * 100.0) / 100.0)
                .timeRequiredSeconds(Math.round(timeSec * 100.0) / 100.0)
                .maintainabilityIndex(Math.round(normalizedMI * 10.0) / 10.0)
                .build();
    }
}
