-- =============================================================================
-- Codexa Security Platform — Baseline Schema Migration V1
-- =============================================================================

CREATE TABLE IF NOT EXISTS analysis_job (
    id UUID PRIMARY KEY,
    source_type VARCHAR(32) NOT NULL,
    source_identifier VARCHAR(1024) NOT NULL,
    repository_commit VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    progress_stage VARCHAR(64),
    progress_percent INT DEFAULT 0,
    overall_score DOUBLE PRECISION,
    verdict VARCHAR(64),
    summary TEXT,
    error_code VARCHAR(64),
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS analysis_metric (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL UNIQUE,
    security_score DOUBLE PRECISION NOT NULL,
    quality_score DOUBLE PRECISION NOT NULL,
    operations_score DOUBLE PRECISION NOT NULL,
    maintainability_score DOUBLE PRECISION NOT NULL,
    architectural_score DOUBLE PRECISION NOT NULL DEFAULT 100.0,
    total_files INT NOT NULL,
    analyzed_files INT NOT NULL,
    critical_count INT NOT NULL,
    high_count INT NOT NULL,
    medium_count INT NOT NULL,
    low_count INT NOT NULL,
    duration_ms BIGINT NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_analysis_metric_job FOREIGN KEY (job_id) REFERENCES analysis_job(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS finding (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    rule_id VARCHAR(64) NOT NULL,
    category VARCHAR(32) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    confidence VARCHAR(32) NOT NULL,
    title VARCHAR(256) NOT NULL,
    description TEXT,
    impact TEXT,
    remediation TEXT,
    owasp_mapping VARCHAR(128),
    file_path VARCHAR(1024) NOT NULL,
    start_line INT,
    end_line INT,
    evidence_masked TEXT,
    suggested_fix TEXT,
    priority_score DOUBLE PRECISION,
    requires_manual_review BOOLEAN NOT NULL DEFAULT FALSE,
    deduplication_hash VARCHAR(64),
    CONSTRAINT fk_finding_job FOREIGN KEY (job_id) REFERENCES analysis_job(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_finding_job_id ON finding(job_id);
CREATE INDEX IF NOT EXISTS idx_finding_category ON finding(category);
CREATE INDEX IF NOT EXISTS idx_finding_severity ON finding(severity);

CREATE TABLE IF NOT EXISTS finding_references (
    finding_id UUID NOT NULL,
    reference_url VARCHAR(2048) NOT NULL,
    CONSTRAINT fk_finding_references_finding FOREIGN KEY (finding_id) REFERENCES finding(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS llm_cache (
    cache_key VARCHAR(64) PRIMARY KEY,
    provider VARCHAR(32) NOT NULL,
    model VARCHAR(64) NOT NULL,
    response_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE
);
