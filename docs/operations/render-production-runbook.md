# Render Production Operations Runbook

## 1. Production Service Architecture
Codexa is deployed to **Render Cloud PaaS** as a unified containerized Java 21 / Spring Boot 3 service serving the bundled Vite React frontend.

* **Live Production URL**: `https://codexa-ye85.onrender.com`
* **Health Check Probe**: `https://codexa-ye85.onrender.com/actuator/health`
* **Swagger OpenAPI UI**: `https://codexa-ye85.onrender.com/swagger-ui.html`

---

## 2. Key Environment Variables

| Variable | Description | Production Default |
| :--- | :--- | :--- |
| `SERVER_PORT` | HTTP Port for Spring Boot | `8080` (Render overrides to `10000`) |
| `SPRING_PROFILES_ACTIVE` | Active configuration profile | `prod` |
| `CODEXA_AI_ENABLED` | Enables AI fallback review | `true` |
| `OPENROUTER_API_KEY` | API token for OpenRouter models | (Configured in Render Secrets) |
| `MAX_FILE_SIZE_MB` | Maximum ZIP archive upload limit | `100` |
| `MAX_CONCURRENT_JOBS` | Maximum concurrent analysis jobs | `10` |

---

## 3. Operational Procedures

### Health & Liveness Verification
```bash
curl -I https://codexa-ye85.onrender.com/actuator/health
# Expected: HTTP/2 200 OK {"status":"UP"}
```

### Zero-Downtime Deployment
Deployments are automated via GitHub Webhook on pushes to `main`. Render builds the Docker image and transitions traffic only after the health probe returns HTTP 200.
