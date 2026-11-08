package com.codexa.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Vulnerability Blast Radius Report.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlastRadiusReport {
    private String findingRuleId;
    private String targetMethod;
    private int upstreamRouteCount;
    private Set<String> exposedEndpoints;
    private int shortestHopCount;
    private double blastRadiusScore;
    private List<String> ingressTrace;
}
