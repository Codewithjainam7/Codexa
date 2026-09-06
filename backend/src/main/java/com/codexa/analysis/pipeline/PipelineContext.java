package com.codexa.analysis.pipeline;

import com.codexa.analysis.model.FindingResponse;
import com.codexa.analysis.model.ProductionVerdict;
import com.codexa.persistence.entity.FindingEntity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PipelineContext {

    private final UUID jobId;
    private final Path stagingDirectory;
    private List<Path> sourceFiles = new ArrayList<>();
    private List<com.codexa.security.ast.ParsedJavaFile> parsedJavaFiles = new ArrayList<>();
    private List<FindingEntity> findings = new ArrayList<>();
    private double securityScore = 100.0;
    private double qualityScore = 100.0;
    private double operationsScore = 100.0;
    private double maintainabilityScore = 100.0;
    private double architecturalScore = 100.0;
    private double overallScore = 100.0;
    private ProductionVerdict verdict = ProductionVerdict.REVIEW_COMPLETE;
    private String executiveSummary = "";
    private int totalFiles = 0;
    private int analyzedFiles = 0;

    public PipelineContext(UUID jobId, Path stagingDirectory) {
        this.jobId = jobId;
        this.stagingDirectory = stagingDirectory;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Path getStagingDirectory() {
        return stagingDirectory;
    }

    public List<Path> getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(List<Path> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public List<com.codexa.security.ast.ParsedJavaFile> getParsedJavaFiles() {
        return parsedJavaFiles;
    }

    public void setParsedJavaFiles(List<com.codexa.security.ast.ParsedJavaFile> parsedJavaFiles) {
        this.parsedJavaFiles = parsedJavaFiles;
    }

    public List<FindingEntity> getFindings() {
        return findings;
    }

    public void setFindings(List<FindingEntity> findings) {
        this.findings = findings;
    }

    public void addFinding(FindingEntity finding) {
        this.findings.add(finding);
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

    public double getMaintainabilityScore() {
        return maintainabilityScore;
    }

    public void setMaintainabilityScore(double maintainabilityScore) {
        this.maintainabilityScore = maintainabilityScore;
    }

    public double getArchitecturalScore() {
        return architecturalScore;
    }

    public void setArchitecturalScore(double architecturalScore) {
        this.architecturalScore = architecturalScore;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public ProductionVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(ProductionVerdict verdict) {
        this.verdict = verdict;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
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
}
