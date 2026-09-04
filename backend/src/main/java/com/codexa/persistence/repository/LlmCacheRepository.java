package com.codexa.persistence.repository;

import com.codexa.persistence.entity.LlmCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LlmCacheRepository extends JpaRepository<LlmCacheEntity, String> {
    Optional<LlmCacheEntity> findByCacheKey(String cacheKey);
}
