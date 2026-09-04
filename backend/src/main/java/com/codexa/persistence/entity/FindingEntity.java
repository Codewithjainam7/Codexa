package com.codexa.persistence.entity;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Confidence;
import com.codexa.analysis.model.Severity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "finding", indexes = {
        @Index(name = "idx_finding_job_id", columnList = "job_id"),
        @Index(name = "idx_finding_category", columnList = "category"),
        @Index(name = "idx_finding_severity", columnList = "severity")
})
public class FindingEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private AnalysisJobEntity job;

    @Column(name = "rule_id", nullable = false, length = 64)
    private String ruleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 32)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence", nullable = false, length = 32)
    private Confidence confidence;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "impact", columnDefinition = "TEXT")
    private String impact;

    @Column(name = "remediation", columnDefinition = "TEXT")
    private String remediation;

    @Column(name = "owasp_mapping", length = 128)
    private String owaspMapping;

    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    @Column(name = "start_line")
    private int startLine;

    @Column(name = "end_line")
    private int endLine;

    @Column(name = "evidence_masked", columnDefinition = "TEXT")
    private String evidenceMasked;

    @Column(name = "suggested_fix", columnDefinition = "TEXT")
    private String suggestedFix;

    @Column(name = "priority_score")
    private double priorityScore;

    @Column(name = "requires_manual_review")
    private boolean requiresManualReview;

    @ElementCollection
    @CollectionTable(name = "finding_references", joinColumns = @JoinColumn(name = "finding_id"))
    @Column(name = "reference_url")
    private List<String> references = new ArrayList<>();

    @Column(name = "deduplication_hash", length = 64)
    private String deduplicationHash;

    public FindingEntity() {
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

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Confidence getConfidence() {
        return confidence;
    }

    public void setConfidence(Confidence confidence) {
        this.confidence = confidence;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getRemediation() {
        return remediation;
    }

    public void setRemediation(String remediation) {
        this.remediation = remediation;
    }

    public String getOwaspMapping() {
        return owaspMapping;
    }

    public void setOwaspMapping(String owaspMapping) {
        this.owaspMapping = owaspMapping;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getEvidenceMasked() {
        return evidenceMasked;
    }

    public void setEvidenceMasked(String evidenceMasked) {
        this.evidenceMasked = evidenceMasked;
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }

    public void setSuggestedFix(String suggestedFix) {
        this.suggestedFix = suggestedFix;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public boolean isRequiresManualReview() {
        return requiresManualReview;
    }

    public void setRequiresManualReview(boolean requiresManualReview) {
        this.requiresManualReview = requiresManualReview;
    }

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }

    public String getDeduplicationHash() {
        return deduplicationHash;
    }

    public void setDeduplicationHash(String deduplicationHash) {
        this.deduplicationHash = deduplicationHash;
    }
}
