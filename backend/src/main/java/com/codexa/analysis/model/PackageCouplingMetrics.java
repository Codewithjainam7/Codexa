package com.codexa.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Robert C. Martin Package Coupling & Instability Metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageCouplingMetrics {

    public record PackageStats(String packageName, int afferentCoupling, int efferentCoupling, double instability) {}

    @Builder.Default
    private Map<String, PackageStats> packageMetrics = new HashMap<>();

    @Builder.Default
    private List<List<String>> circularDependencyCycles = new ArrayList<>();

    public boolean hasCircularDependencies() {
        return !circularDependencyCycles.isEmpty();
    }
}
