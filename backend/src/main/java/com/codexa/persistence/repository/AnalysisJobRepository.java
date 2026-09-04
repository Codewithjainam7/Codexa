package com.codexa.persistence.repository;

import com.codexa.persistence.entity.AnalysisJobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnalysisJobRepository extends JpaRepository<AnalysisJobEntity, UUID> {
    Page<AnalysisJobEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
