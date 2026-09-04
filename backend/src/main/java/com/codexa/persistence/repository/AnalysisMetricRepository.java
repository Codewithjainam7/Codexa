package com.codexa.persistence.repository;

import com.codexa.persistence.entity.AnalysisMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisMetricRepository extends JpaRepository<AnalysisMetricEntity, UUID> {
    Optional<AnalysisMetricEntity> findByJob_Id(UUID jobId);
}
