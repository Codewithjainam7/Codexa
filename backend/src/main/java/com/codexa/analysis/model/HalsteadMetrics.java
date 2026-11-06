package com.codexa.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Halstead Software Science Metrics & Maintainability Index model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HalsteadMetrics {

    /** Number of distinct operators (eta1) */
    private int distinctOperators;

    /** Number of distinct operands (eta2) */
    private int distinctOperands;

    /** Total number of operators (N1) */
    private int totalOperators;

    /** Total number of operands (N2) */
    private int totalOperands;

    /** Program Vocabulary: eta = eta1 + eta2 */
    private int programVocabulary;

    /** Program Length: N = N1 + N2 */
    private int programLength;

    /** Program Volume: V = N * log2(eta) */
    private double volume;

    /** Difficulty: D = (eta1 / 2) * (N2 / eta2) */
    private double difficulty;

    /** Effort: E = D * V */
    private double effort;

    /** Time to implement (seconds): T = E / 18 */
    private double timeRequiredSeconds;

    /** Software Engineering Institute (SEI) Maintainability Index (0 - 100) */
    private double maintainabilityIndex;
}
