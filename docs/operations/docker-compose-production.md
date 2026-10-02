# Multi-Container Docker Compose Production Orchestration Guide

## 1. Overview
For self-hosted enterprise deployments, Codexa provides a turnkey `docker-compose.yml` orchestrating the backend API, frontend web portal, and Prometheus monitoring.

---

## 2. Production Compose Specification

```yaml
version: '3.8'

services:
  codexa-app:
    image: ghcr.io/codewithjainam7/codexa:latest
    container_name: codexa-production
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SERVER_PORT=8080
      - MAX_FILE_SIZE_MB=100
    volumes:
      - codexa-data:/app/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 15s
      timeout: 5s
      retries: 3

volumes:
  codexa-data:
    driver: local
```
