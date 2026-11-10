package com.codexa.analysis.service;

import com.codexa.analysis.model.FanMetrics;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calculates Henry & Kafura structural complexity metrics:
 * Fan-In (inflow of information/callers) and Fan-Out (outflow/callees).
 * Structural Complexity = LOC * (FanIn * FanOut)^2.
 */
@Service
public class FanInFanOutCalculator {

    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile(
            "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\("
    );

    private static final Pattern METHOD_DECL_PATTERN = Pattern.compile(
            "(?:public|protected|private|static|\\s)+[\\w<>\\[\\]]+\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\([^)]*\\)\\s*\\{?"
    );

    /**
     * Calculates Fan-In, Fan-Out, and Structural Complexity for methods in source code.
     */
    public List<FanMetrics> computeMetrics(String sourceCode, String className) {
        List<FanMetrics> results = new ArrayList<>();
        if (sourceCode == null || sourceCode.isBlank()) {
            return results;
        }

        String[] lines = sourceCode.split("\\r?\\n");
        int totalLoc = lines.length;

        // Collect all declared methods
        Set<String> declaredMethods = new HashSet<>();
        Matcher declMatcher = METHOD_DECL_PATTERN.matcher(sourceCode);
        while (declMatcher.find()) {
            String mName = declMatcher.group(1);
            if (!isReservedKeyword(mName)) {
                declaredMethods.add(mName);
            }
        }

        // Count calls
        Map<String, Integer> callFrequencies = new HashMap<>();
        Matcher callMatcher = METHOD_CALL_PATTERN.matcher(sourceCode);
        int totalOutboundCalls = 0;

        while (callMatcher.find()) {
            String calledName = callMatcher.group(1);
            if (!isReservedKeyword(calledName)) {
                callFrequencies.put(calledName, callFrequencies.getOrDefault(calledName, 0) + 1);
                totalOutboundCalls++;
            }
        }

        for (String method : declaredMethods) {
            int fanIn = callFrequencies.getOrDefault(method, 0);
            int fanOut = Math.max(1, totalOutboundCalls / Math.max(1, declaredMethods.size()));
            int loc = Math.max(5, totalLoc / Math.max(1, declaredMethods.size()));

            // Henry & Kafura IFC = LOC * (Fan-In * Fan-Out)^2
            double structuralComplexity = loc * Math.pow((double) fanIn * fanOut, 2);
            boolean isBottleneck = structuralComplexity > 5000.0 || (fanIn > 15 && fanOut > 10);

            results.add(new FanMetrics(
                    className + "." + method,
                    fanIn,
                    fanOut,
                    loc,
                    structuralComplexity,
                    isBottleneck
            ));
        }

        return results;
    }

    private boolean isReservedKeyword(String name) {
        return Set.of("if", "for", "while", "switch", "catch", "return", "synchronized", "super", "this", "new")
                .contains(name);
    }
}
