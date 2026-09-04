package com.codexa.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "llm_cache")
public class LlmCacheEntity {

    @Id
    @Column(name = "cache_key", length = 64)
    private String cacheKey;

    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "model", nullable = false, length = 64)
    private String model;

    @Column(name = "response_json", nullable = false, columnDefinition = "TEXT")
    private String responseJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public LlmCacheEntity() {
    }

    public LlmCacheEntity(String cacheKey, String provider, String model, String responseJson, Instant createdAt, Instant expiresAt) {
        this.cacheKey = cacheKey;
        this.provider = provider;
        this.model = model;
        this.responseJson = responseJson;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
