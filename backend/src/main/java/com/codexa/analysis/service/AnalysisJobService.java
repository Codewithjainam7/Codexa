package com.codexa.analysis.service;

import com.codexa.analysis.model.*;
import com.codexa.common.error.ApiException;
import com.codexa.persistence.entity.AnalysisJobEntity;
import com.codexa.persistence.entity.AnalysisMetricEntity;
import com.codexa.persistence.entity.FindingEntity;
import com.codexa.persistence.repository.AnalysisJobRepository;
import com.codexa.persistence.repository.AnalysisMetricRepository;
import com.codexa.persistence.repository.FindingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnalysisJobService {

    private final AnalysisJobRepository jobRepository;
    private final FindingRepository findingRepository;
    private final AnalysisMetricRepository metricRepository;

    public AnalysisJobService(
            AnalysisJobRepository jobRepository,
            FindingRepository findingRepository,
            AnalysisMetricRepository metricRepository
    ) {
        this.jobRepository = jobRepository;
        this.findingRepository = findingRepository;
        this.metricRepository = metricRepository;
    }

    @Transactional
    public AnalysisJobEntity createJob(SourceType sourceType, String identifier) {
        AnalysisJobEntity entity = new AnalysisJobEntity(UUID.randomUUID(), sourceType, identifier);
        return jobRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public AnalysisJobEntity getJobOrThrow(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "JOB_NOT_FOUND", "Analysis job not found: " + jobId));
    }

    @Transactional(readOnly = true)
    public AnalysisJobResponse getJobResponse(UUID jobId) {
        AnalysisJobEntity entity = getJobOrThrow(jobId);
        AnalysisMetricResponse metricResponse = null;

        if (entity.getMetric() != null) {
            AnalysisMetricEntity m = entity.getMetric();
            metricResponse = new AnalysisMetricResponse(
                    m.getSecurityScore(),
                    m.getQualityScore(),
                    m.getOperationsScore(),
                    m.getTotalFiles(),
                    m.getAnalyzedFiles(),
                    m.getCriticalCount(),
                    m.getHighCount(),
                    m.getMediumCount(),
                    m.getLowCount(),
                    m.getDurationMs()
            );
        }

        List<FindingEntity> topEntities = findingRepository.findByJob_IdOrderByPriorityScoreDesc(jobId);
        List<FindingResponse> topFindings = topEntities.stream()
                .limit(5)
                .map(this::toFindingResponse)
                .collect(Collectors.toList());

        return new AnalysisJobResponse(
                entity.getId(),
                entity.getSourceType(),
                entity.getSourceIdentifier(),
                entity.getRepositoryCommit(),
                entity.getStatus(),
                entity.getProgressStage(),
                entity.getProgressPercent(),
                entity.getOverallScore(),
                entity.getVerdict(),
                entity.getSummary(),
                entity.getErrorCode(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getCompletedAt(),
                metricResponse,
                topFindings
        );
    }

    @Transactional(readOnly = true)
    public Page<FindingResponse> getFindings(
            UUID jobId,
            Category category,
            Severity severity,
            Confidence confidence,
            String search,
            int page,
            int size
    ) {
        // verify job exists
        getJobOrThrow(jobId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "priorityScore"));

        Specification<FindingEntity> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("job").get("id"), jobId));

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }
            if (confidence != null) {
                predicates.add(cb.equal(root.get("confidence"), confidence));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("filePath")), pattern),
                        cb.like(cb.lower(root.get("ruleId")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return findingRepository.findAll(spec, pageable).map(this::toFindingResponse);
    }

    @Transactional(readOnly = true)
    public AnalysisReportResponse generateReport(UUID jobId) {
        AnalysisJobEntity entity = getJobOrThrow(jobId);
        List<FindingEntity> findings = findingRepository.findByJob_IdOrderByPriorityScoreDesc(jobId);

        AnalysisMetricResponse metricResponse = null;
        if (entity.getMetric() != null) {
            AnalysisMetricEntity m = entity.getMetric();
            metricResponse = new AnalysisMetricResponse(
                    m.getSecurityScore(),
                    m.getQualityScore(),
                    m.getOperationsScore(),
                    m.getTotalFiles(),
                    m.getAnalyzedFiles(),
                    m.getCriticalCount(),
                    m.getHighCount(),
                    m.getMediumCount(),
                    m.getLowCount(),
                    m.getDurationMs()
            );
        }

        List<FindingResponse> findingResponses = findings.stream()
                .map(this::toFindingResponse)
                .toList();

        return new AnalysisReportResponse(
                entity.getId(),
                "Codexa Code Review & Security Audit",
                entity.getSourceIdentifier(),
                entity.getSourceType(),
                entity.getOverallScore(),
                entity.getVerdict(),
                entity.getSummary(),
                entity.getCreatedAt(),
                metricResponse,
                findingResponses,
                AnalysisReportResponse.STANDARD_DISCLAIMER
        );
    }

    @Transactional
    public void updateProgress(UUID jobId, JobStatus status, String progressStage, int progressPercent) {
        AnalysisJobEntity entity = getJobOrThrow(jobId);
        entity.setStatus(status);
        entity.setProgressStage(progressStage);
        entity.setProgressPercent(progressPercent);
        jobRepository.save(entity);
    }

    @Transactional
    public void markJobFailed(UUID jobId, String errorCode, String errorMessage) {
        AnalysisJobEntity entity = getJobOrThrow(jobId);
        entity.setStatus(JobStatus.FAILED);
        entity.setErrorCode(errorCode);
        entity.setErrorMessage(errorMessage);
        entity.setCompletedAt(Instant.now());
        jobRepository.save(entity);
    }

    @Transactional
    public void completeJob(
            UUID jobId,
            double overallScore,
            ProductionVerdict verdict,
            String summary,
            List<FindingEntity> findings,
            double secScore,
            double qualScore,
            double opsScore,
            int totalFiles,
            int analyzedFiles,
            long durationMs
    ) {
        AnalysisJobEntity entity = getJobOrThrow(jobId);
        entity.setStatus(JobStatus.COMPLETED);
        entity.setProgressStage("COMPLETED");
        entity.setProgressPercent(100);
        entity.setOverallScore(overallScore);
        entity.setVerdict(verdict);
        entity.setSummary(summary);
        entity.setCompletedAt(Instant.now());

        // Save findings
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;

        for (FindingEntity f : findings) {
            f.setJob(entity);
            if (f.getSeverity() == Severity.CRITICAL) criticalCount++;
            else if (f.getSeverity() == Severity.HIGH) highCount++;
            else if (f.getSeverity() == Severity.MEDIUM) mediumCount++;
            else if (f.getSeverity() == Severity.LOW) lowCount++;
            findingRepository.save(f);
        }

        // Save metric
        AnalysisMetricEntity metric = new AnalysisMetricEntity();
        metric.setId(UUID.randomUUID());
        metric.setJob(entity);
        metric.setSecurityScore(secScore);
        metric.setQualityScore(qualScore);
        metric.setOperationsScore(opsScore);
        metric.setTotalFiles(totalFiles);
        metric.setAnalyzedFiles(analyzedFiles);
        metric.setCriticalCount(criticalCount);
        metric.setHighCount(highCount);
        metric.setMediumCount(mediumCount);
        metric.setLowCount(lowCount);
        metric.setDurationMs(durationMs);
        metric.setRecordedAt(Instant.now());

        metricRepository.save(metric);
        entity.setMetric(metric);

        jobRepository.save(entity);
    }

    private FindingResponse toFindingResponse(FindingEntity f) {
        return new FindingResponse(
                f.getId(),
                f.getRuleId(),
                f.getCategory(),
                f.getSeverity(),
                f.getConfidence(),
                f.getTitle(),
                f.getDescription(),
                f.getImpact(),
                f.getRemediation(),
                f.getOwaspMapping(),
                f.getFilePath(),
                f.getStartLine(),
                f.getEndLine(),
                f.getEvidenceMasked(),
                f.getSuggestedFix(),
                f.getPriorityScore(),
                f.isRequiresManualReview(),
                f.getReferences()
        );
    }
}
