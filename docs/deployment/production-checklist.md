# Production Deployment Checklist

1. Set strict OPENROUTER_API_KEY environment variable.
2. Configure PostgreSQL or persistent relational database.
3. Enable TLS terminating reverse proxy.
4. Validate staging directory cleanup sweeps.


### Container Warmup & Health Check Strategy

- Ensure `/api/v1/health` responds with `200 OK`.
- Keep-alive ping crons can be configured to prevent cold-sleep on Render free tiers.
- Enable graceful shutdown in `server.shutdown=graceful` for in-flight analyses.
