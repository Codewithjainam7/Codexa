# Docker & Containerized Deployment Guide

Codexa supports containerized deployment using multi-stage Docker builds that bundle the Vite React frontend and Spring Boot Java backend into a lightweight, non-root, hardened container image.

---

## 1. Multi-Stage Dockerfile Architecture

```dockerfile
# Stage 1: Build Vite React Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY backend/src ./src
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static
RUN mvn clean package -DskipTests -B

# Stage 3: Minimal Distroless / Hardened Runtime
FROM eclipse-temurin:21-jre-alpine AS runner
LABEL maintainer="Codexa Engineering <dev@codexa.dev>"

# Security: Run as unprivileged non-root user
RUN addgroup -S codexa && adduser -S codexa -G codexa
USER codexa:codexa
WORKDIR /app

# Copy executable jar
COPY --from=backend-builder --chown=codexa:codexa /app/backend/target/codexa-backend-*.jar app.jar

# JVM Container Tuning: Auto-detect cgroup memory limits
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## 2. Docker Compose Local Orchestration

Create `docker-compose.yml` in the root repository:

```yaml
version: '3.8'

services:
  codexa:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: codexa-platform
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - OPENROUTER_API_KEY=${OPENROUTER_API_KEY:-}
      - CODEXA_MAX_ARCHIVE_SIZE=3221225472 # 3GB
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 4096M
          cpus: '2.0'
        reservations:
          memory: 1024M
```

---

## 3. Container Management Commands

### Build & Run Locally
```bash
# Build production image
docker build -t codexa:latest .

# Run container with 2GB RAM allocation
docker run -d --name codexa -p 8080:8080 -m 2g codexa:latest

# Check live logs
docker logs -f codexa
```

### Health Check Verification
```bash
docker inspect --format='{{json .State.Health.Status}}' codexa
# Returns: "healthy"
```
