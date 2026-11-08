package com.codexa.analysis.service;

import com.codexa.analysis.model.BlastRadiusReport;
import com.codexa.analysis.model.CallGraph;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Calculates the upstream impact and exposure blast radius of identified vulnerabilities.
 */
@Service
public class BlastRadiusCalculator {

    public BlastRadiusReport calculate(CallGraph callGraph, String targetMethodKey, Set<String> publicEndpoints) {
        if (callGraph == null || targetMethodKey == null) {
            return BlastRadiusReport.builder().blastRadiusScore(0.1).build();
        }

        // Build reverse adjacency list (callee -> callers)
        Map<String, List<String>> reverseAdj = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : callGraph.getAdjacencyList().entrySet()) {
            String caller = entry.getKey();
            for (String callee : entry.getValue()) {
                reverseAdj.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
            }
        }

        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(targetMethodKey);
        visited.add(targetMethodKey);

        Set<String> impactedEndpoints = new HashSet<>();
        int shortestHop = Integer.MAX_VALUE;
        Map<String, Integer> distance = new HashMap<>();
        distance.put(targetMethodKey, 0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDist = distance.getOrDefault(current, 0);

            if (publicEndpoints != null && publicEndpoints.contains(current)) {
                impactedEndpoints.add(current);
                shortestHop = Math.min(shortestHop, currentDist);
            }

            for (String caller : reverseAdj.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(caller)) {
                    visited.add(caller);
                    distance.put(caller, currentDist + 1);
                    queue.add(caller);
                }
            }
        }

        double score = 0.2;
        if (!impactedEndpoints.isEmpty()) {
            score = Math.min(1.0, 0.4 + (impactedEndpoints.size() * 0.15) + (1.0 / Math.max(1, shortestHop)));
        }

        return BlastRadiusReport.builder()
                .targetMethod(targetMethodKey)
                .upstreamRouteCount(impactedEndpoints.size())
                .exposedEndpoints(impactedEndpoints)
                .shortestHopCount(shortestHop == Integer.MAX_VALUE ? -1 : shortestHop)
                .blastRadiusScore(Math.round(score * 100.0) / 100.0)
                .build();
    }
}
