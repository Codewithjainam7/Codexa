package com.codexa.persistence.repository;

import com.codexa.analysis.model.Category;
import com.codexa.analysis.model.Severity;
import com.codexa.persistence.entity.FindingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FindingRepository extends JpaRepository<FindingEntity, UUID>, JpaSpecificationExecutor<FindingEntity> {

    List<FindingEntity> findByJob_IdOrderByPriorityScoreDesc(UUID jobId);

    Page<FindingEntity> findByJob_Id(UUID jobId, Pageable pageable);

    Page<FindingEntity> findByJob_IdAndCategory(UUID jobId, Category category, Pageable pageable);

    Page<FindingEntity> findByJob_IdAndSeverity(UUID jobId, Severity severity, Pageable pageable);

    Page<FindingEntity> findByJob_IdAndCategoryAndSeverity(UUID jobId, Category category, Severity severity, Pageable pageable);
}
