# Codexa Nginx Reverse Proxy Configuration

## Overview
For production on-premise deployments, Nginx terminates TLS, buffers large multipart zip uploads, and serves cached static assets.

## Sample Nginx Virtual Host Configuration

```nginx
server {
    listen 443 ssl http2;
    server_name codexa.corp.internal;

    ssl_certificate /etc/ssl/certs/codexa.crt;
    ssl_certificate_key /etc/ssl/private/codexa.key;
    ssl_protocols TLSv1.2 TLSv1.3;

    # Maximum upload size for zip archives
    client_max_body_size 100M;
    client_body_buffer_size 10M;

    # Backend API proxy
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # SSE Streaming support
        proxy_set_header Connection '';
        proxy_http_version 1.1;
        chunked_transfer_encoding off;
        proxy_buffering off;
        proxy_cache off;
    }

    # Static Web UI frontend
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_intercept_errors on;
        error_page 404 = /index.html;
    }
}
```
