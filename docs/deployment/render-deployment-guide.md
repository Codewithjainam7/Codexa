# Deploying Codexa on Render

This guide outlines how to deploy Codexa as a continuous-deployment Web Service on Render using Docker or native Java runtime.

---

## 1. Quick Deploy with `render.yaml`

Add or verify `render.yaml` in your repository root:

```yaml
services:
  - type: web
    name: codexa-platform
    env: docker
    plan: standard
    region: oregon
    branch: main
    healthCheckPath: /actuator/health
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: PORT
        value: 8080
      - key: JAVA_OPTS
        value: "-Xmx1536m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
      - key: OPENROUTER_API_KEY
        sync: false
    disk:
      name: codexa-staging
      mountPath: /app/.staging
      sizeGB: 10
```

---

## 2. Render Dashboard Manual Setup

1. **Create Web Service**: Connect your GitHub repository (`Codewithjainam7/Codexa`).
2. **Environment**: Select **Docker**.
3. **Instance Type**:
   - Minimum: **Starter** (512MB RAM - suitable for small repos < 100 files).
   - Recommended: **Standard** (2GB RAM - handles 3GB ZIPs and large AST parsing).
4. **Health Check Path**: Set to `/actuator/health`.
5. **Add Environment Variables**:
   - `OPENROUTER_API_KEY`: Secret API key for AI remediation enrichment.
   - `SPRING_PROFILES_ACTIVE`: `prod`

---

## 3. Free-Tier Cold Start Prevention (Keep-Alive Ping)

On Render free tiers, services spin down after 15 minutes of inactivity. You can configure a lightweight keep-alive ping:
- Set up a scheduled cron job (e.g. GitHub Actions or Cron-Job.org) pinging `GET https://your-service.onrender.com/actuator/health` every 10 minutes.
- Ensures immediate sub-second response times without cold-start JVM compilation latencies.

---

## 4. Verification & Status Monitoring

After Render completes the build and deployment:
```bash
# Verify health endpoint
curl -i https://your-service.onrender.com/actuator/health

# Trigger test audit of public repository
curl -X POST https://your-service.onrender.com/api/v1/analyses/github \
  -H "Content-Type: application/json" \
  -d '{"repoUrl":"https://github.com/Codewithjainam7/Codexa"}'
```
