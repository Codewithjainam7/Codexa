package com.codexa.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_metric")
public class AnalysisMetricEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private AnalysisJobEntity job;

    @Column(name = "security_score")
    private double securityScore;

    @Column(name = "quality_score")
    private double qualityScore;

    @Column(name = "operations_score")
    private double operationsScore;

    @Column(name = "total_files")
    private int totalFiles;

    @Column(name = "analyzed_files")
    private int analyzedFiles;

    @Column(name = "critical_count")
    private int criticalCount;

    @Column(name = "high_count")
    private int highCount;

    @Column(name = "medium_count")
    private int mediumCount;

    @Column(name = "low_count")
    private int lowCount;

    @Column(name = "duration_ms")
    private long durationMs;

    @Column(name = "recorded_at")
    private Instant recordedAt;

    public AnalysisMetricEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AnalysisJobEntity getJob() {
        return job;
    }

    public void setJob(AnalysisJobEntity job) {
        this.job = job;
    }

    public double getSecurityScore() {
        return securityScore;
    }

    public void setSecurityScore(double securityScore) {
        this.securityScore = securityScore;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(double qualityScore) {
        this.qualityScore = qualityScore;
    }

    public double getOperationsScore() {
        return operationsScore;
    }

    public void setOperationsScore(double operationsScore) {
        this.operationsScore = operationsScore;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getAnalyzedFiles() {
        return analyzedFiles;
    }

    public void setAnalyzedFiles(int analyzedFiles) {
        this.analyzedFiles = analyzedFiles;
    }

    public int getCriticalCount() {
        return criticalCount;
    }

    public void setCriticalCount(int criticalCount) {
        this.criticalCount = criticalCount;
    }

    public int getHighCount() {
        return highCount;
    }

    public void setHighCount(int highCount) {
        this.highCount = highCount;
    }

    public int getMediumCount() {
        return mediumCount;
    }

    public void setMediumCount(int mediumCount) {
        this.mediumCount = mediumCount;
    }

    public int getLowCount() {
        return lowCount;
    }

    public void setLowCount(int lowCount) {
        this.lowCount = lowCount;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }
}
