# Codexa Production Readiness & Deployment Checklist

Use this operational checklist to certify that a Codexa instance is hardened, correctly configured, and resilient before promoting to production traffic.

---

## 1. Environment & Secret Management

- [ ] **API Keys & LLM Tokens**: Set `OPENROUTER_API_KEY` or `NVIDIA_API_KEY` via environment variable or secret manager; never commit keys to files.
- [ ] **Spring Profile**: Set `SPRING_PROFILES_ACTIVE=prod`.
- [ ] **Database Connection**: Configure external PostgreSQL instance via `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
- [ ] **Staging Root Directory**: Configure persistent or isolated ephemeral fast-SSD mount: `CODEXA_STAGING_DIR=/var/run/codexa/staging`.

---

## 2. Memory & JVM Configuration

- [ ] **Memory Allocation**: Minimum 2 GB RAM for standard repos; recommend 4 GB+ RAM for 3.0 GB monorepos.
- [ ] **JVM Container Sizing**: Pass `-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC` to prevent JVM OOM within cgroups.
- [ ] **OOM Handling**: Enable `-XX:+ExitOnOutOfMemoryError` so orchestrators (Kubernetes / Render) automatically restart unhealthy pods.

---

## 3. Network, Ingress & Security Perimeter

- [ ] **TLS 1.3 Termination**: Terminate HTTPS at the ingress load balancer (Nginx / Cloudflare / AWS ALB) with HSTS enabled.
- [ ] **Rate Limiting**: Verify `RateLimitingFilter` is active; verify `X-Forwarded-For` trust settings match reverse proxy CIDRs.
- [ ] **SSRF Filters**: Confirm outbound traffic is restricted from resolving private VPC subnets and metadata IP `169.254.169.254`.
- [ ] **Security Headers**: Ensure response headers include `Content-Security-Policy`, `X-Content-Type-Options: nosniff`, and `X-Frame-Options: DENY`.

---

## 4. Observability & Health Probes

- [ ] **Liveness Probe**: Point to `GET /actuator/health/liveness` (returns `200 OK`).
- [ ] **Readiness Probe**: Point to `GET /actuator/health/readiness` (returns `200 OK`).
- [ ] **Metrics Scraping**: Enable Prometheus metrics export at `GET /actuator/prometheus`.
- [ ] **Structured Logging**: Verify logs output in JSON format with ISO-8601 timestamps and masked credentials.

---

## 5. Graceful Shutdown & Ephemeral Cleanup

- [ ] **Graceful Drain**: Set `server.shutdown=graceful` and `spring.lifecycle.timeout-per-shutdown-phase=30s` to allow active AST scans to complete.
- [ ] **Staging Purge**: Verify that interrupted jobs have their `.staging/{uuid}` directories cleaned up on JVM restart.
