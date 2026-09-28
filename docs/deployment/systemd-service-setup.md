# Codexa Linux Systemd Service Setup & Hardening Guide

This guide details the installation, systemd unit configuration, Linux security sandboxing, JVM garbage collection tuning, and service lifecycle management for deploying **Codexa** directly on Linux hosts (Ubuntu, Debian, RHEL, Rocky Linux).

---

## 1. System Preparation & Service User Creation

For least-privilege security compliance, Codexa must run under a dedicated, unprivileged system user without login shell access:

```bash
# 1. Create system user and group
sudo groupadd --system codexa
sudo useradd --system --gid codexa --no-create-home \
    --shell /usr/sbin/nologin --comment "Codexa Static Analysis Service" codexa

# 2. Create required directory tree
sudo mkdir -p /opt/codexa/bin
sudo mkdir -p /etc/codexa
sudo mkdir -p /var/log/codexa
sudo mkdir -p /var/lib/codexa/staging

# 3. Set ownership and strict POSIX permissions
sudo chown -R codexa:codexa /opt/codexa /var/log/codexa /var/lib/codexa
sudo chmod 750 /opt/codexa /var/log/codexa /var/lib/codexa
sudo chmod 700 /var/lib/codexa/staging
```

---

## 2. Environment File (`/etc/codexa/codexa.env`)

Store environment variables and sensitive credentials in a dedicated file with `0600` permissions:

```ini
# /etc/codexa/codexa.env
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Ephemeral Staging Directory
CODEXA_STAGING_BASE_DIR=/var/lib/codexa/staging
CODEXA_STAGING_CLEANUP_ON_COMPLETION=true

# Database Configuration (PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/codexa_db
SPRING_DATASOURCE_USERNAME=codexa_user
SPRING_DATASOURCE_PASSWORD=StrongProductionDatabasePasswordHere

# Optional OpenRouter AI Key
OPENROUTER_API_KEY=sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

```bash
sudo chown root:codexa /etc/codexa/codexa.env
sudo chmod 640 /etc/codexa/codexa.env
```

---

## 3. Hardened Systemd Unit File (`/etc/systemd/system/codexa.service`)

This unit file implements strict Linux kernel sandboxing via systemd directives:

```ini
[Unit]
Description=Codexa Autonomous Code Review Engine
Documentation=https://github.com/Codewithjainam7/Codexa/tree/main/docs
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=codexa
Group=codexa
WorkingDirectory=/opt/codexa

# Read environment variables
EnvironmentFile=/etc/codexa/codexa.env

# JVM Execution with G1GC & Memory Safeguards
ExecStart=/usr/bin/java \
    -server \
    -Xms2048m \
    -Xmx4096m \
    -XX:+UseG1GC \
    -XX:G1ReservePercent=15 \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/var/log/codexa/oom-dump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -jar /opt/codexa/bin/codexa-backend.jar

# Process Management & Lifecycle
Restart=on-failure
RestartSec=10s
TimeoutStopSec=45s
KillMode=mixed
LimitNOFILE=65536
LimitNPROC=8192

# Linux Kernel Sandboxing & Hardening Directives
ProtectSystem=strict
ProtectHome=true
PrivateTmp=true
PrivateDevices=true
ProtectKernelTunables=true
ProtectKernelModules=true
ProtectControlGroups=true
NoNewPrivileges=true
CapabilityBoundingSet=
RestrictAddressFamilies=AF_INET AF_INET6 AF_UNIX
RestrictRealtime=true
RestrictSUIDSGID=true
LockPersonality=true

# Read-Write Mount Paths
ReadWritePaths=/var/log/codexa /var/lib/codexa

[Install]
WantedBy=multi-user.target
```

---

## 4. Lifecycle Management Commands

```bash
# Reload systemd manager configuration
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable codexa.service

# Start the service
sudo systemctl start codexa.service

# Check real-time service status
sudo systemctl status codexa.service

# Follow live structured logs via journalctl
sudo journalctl -u codexa.service -f -o json-pretty
```

---

## 5. Health Check & Validation

Verify the local instance is healthy and responding to actuator probes:

```bash
curl -i http://localhost:8080/actuator/health
```

Expected response:
```http
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json

{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
```
