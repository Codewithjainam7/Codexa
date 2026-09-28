package com.codexa.analysis.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates the complete Software Bill of Materials (SBOM) for an analysis job.
 */
public record SbomReport(
        UUID jobId,
        String projectName,
        String specVersion, // e.g. "1.5" (CycloneDX) or "2.3" (SPDX)
        Instant generatedAt,
        int totalDependencies,
        int vulnerableDependencies,
        List<SbomComponent> components,
        List<CveAdvisory> detectedVulnerabilities
) {}
