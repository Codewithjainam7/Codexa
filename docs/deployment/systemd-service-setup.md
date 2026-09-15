# Codexa Linux Systemd Service Setup

## Unit File: `/etc/systemd/system/codexa.service`

```ini
[Unit]
Description=Codexa Autonomous Code Review Engine
After=network.target

[Service]
Type=simple
User=codexa
Group=codexa
WorkingDirectory=/opt/codexa
ExecStart=/usr/bin/java -Xms1024m -Xmx2048m -jar /opt/codexa/codexa-backend.jar
Restart=on-failure
RestartSec=10
LimitNOFILE=65536
Environment="CODEXA_OFFLINE_MODE=true"
Environment="SERVER_PORT=8080"

[Install]
WantedBy=multi-user.target
```

## Lifecycle Commands
```bash
sudo systemctl daemon-reload
sudo systemctl enable codexa
sudo systemctl start codexa
sudo journalctl -u codexa -f
```
