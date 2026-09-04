package com.codexa.persistence.entity;

import com.codexa.analysis.model.JobStatus;
import com.codexa.analysis.model.ProductionVerdict;
import com.codexa.analysis.model.SourceType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "analysis_job")
public class AnalysisJobEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private SourceType sourceType;

    @Column(name = "source_identifier", nullable = false, length = 1024)
    private String sourceIdentifier;

    @Column(name = "repository_commit", length = 128)
    private String repositoryCommit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private JobStatus status;

    @Column(name = "progress_stage", length = 64)
    private String progressStage;

    @Column(name = "progress_percent")
    private int progressPercent;

    @Column(name = "overall_score")
    private Double overallScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", length = 64)
    private ProductionVerdict verdict;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FindingEntity> findings = new ArrayList<>();

    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private AnalysisMetricEntity metric;

    public AnalysisJobEntity() {
    }

    public AnalysisJobEntity(UUID id, SourceType sourceType, String sourceIdentifier) {
        this.id = id != null ? id : UUID.randomUUID();
        this.sourceType = sourceType;
        this.sourceIdentifier = sourceIdentifier;
        this.status = JobStatus.QUEUED;
        this.progressStage = "QUEUED";
        this.progressPercent = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    public String getRepositoryCommit() {
        return repositoryCommit;
    }

    public void setRepositoryCommit(String repositoryCommit) {
        this.repositoryCommit = repositoryCommit;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getProgressStage() {
        return progressStage;
    }

    public void setProgressStage(String progressStage) {
        this.progressStage = progressStage;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public ProductionVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(ProductionVerdict verdict) {
        this.verdict = verdict;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<FindingEntity> getFindings() {
        return findings;
    }

    public void setFindings(List<FindingEntity> findings) {
        this.findings = findings;
    }

    public AnalysisMetricEntity getMetric() {
        return metric;
    }

    public void setMetric(AnalysisMetricEntity metric) {
        this.metric = metric;
    }
}
