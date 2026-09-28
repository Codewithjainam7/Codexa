# Codexa AWS ECS Fargate Production Deployment Guide

This guide details the architecture, AWS CloudFormation/Terraform manifests, task definitions, and operational runbooks for deploying **Codexa** to **AWS ECS Fargate** with serverless container infrastructure.

---

## 1. Cloud Architecture Topology

```
                                [ Internet Clients ]
                                         │
                                         ▼
                           [ Route 53 (codexa.company.com) ]
                                         │
                                         ▼
                     [ Application Load Balancer (ALB) ]
                   (HTTPS 443 with AWS Certificate Manager)
                                         │
                ┌────────────────────────┴────────────────────────┐
                │ Private Subnet A              Private Subnet B  │
                ▼                               ▼                 │
     [ ECS Fargate Task 1 ]           [ ECS Fargate Task 2 ]      │
       (2 vCPU / 4 GB RAM)              (2 vCPU / 4 GB RAM)       │
                │                               │                 │
                └───────────────┬───────────────┘                 │
                                │                                 │
                                ▼                                 ▼
               [ Aurora PostgreSQL Serverless v2 ]    [ AWS Secrets Manager ]
                     (Multi-AZ, Encrypted)           (DB credentials & API keys)
```

### Key Architectural Benefits:
- **Zero EC2 Host Management**: AWS manages underlying server instances, security patching, and OS upgrades.
- **Serverless Autoscaling**: Tasks automatically scale between 2 and 10 replicas based on incoming scan requests.
- **VPC Isolation**: Tasks reside in private subnets with NAT Gateway egress for GitHub cloning and OpenRouter API calls.

---

## 2. ECS Fargate Task Definition (`task-definition.json`)

```json
{
  "family": "codexa-backend-prod",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "2048",
  "memory": "4096",
  "executionRoleArn": "arn:aws:iam::123456789012:role/codexa-ecs-execution-role",
  "taskRoleArn": "arn:aws:iam::123456789012:role/codexa-ecs-task-role",
  "containerDefinitions": [
    {
      "name": "codexa-backend",
      "image": "ghcr.io/codewithjainam7/codexa:1.3.0",
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"},
        {"name": "CODEXA_STAGING_CLEANUP_ON_COMPLETION", "value": "true"},
        {"name": "CODEXA_AI_ENABLED", "value": "true"}
      ],
      "secrets": [
        {
          "name": "SPRING_DATASOURCE_URL",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:codexa/prod/db-url"
        },
        {
          "name": "SPRING_DATASOURCE_USERNAME",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:codexa/prod/db-user"
        },
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:codexa/prod/db-pass"
        },
        {
          "name": "OPENROUTER_API_KEY",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:codexa/prod/openrouter-key"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/codexa-prod",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "backend"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
        "interval": 15,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 30
      }
    }
  ]
}
```

---

## 3. ALB Target Group & Health Check Configuration

Configure the ALB target group to route traffic to container port `8080`:

- **Protocol**: HTTP
- **Port**: 8080
- **Target Type**: IP (required for Fargate awsvpc mode)
- **Health Check Path**: `/actuator/health/readiness`
- **Healthy Threshold**: 2
- **Unhealthy Threshold**: 3
- **Timeout**: 5 seconds
- **Interval**: 10 seconds
- **Success Codes**: 200

---

## 4. Application Auto-Scaling Policies

Configure AWS Application Auto Scaling to adjust task replica counts automatically:

### Target Tracking Scaling Policy (CPU Utilization):
```bash
aws application-autoscaling put-scaling-policy \
  --policy-name codexa-cpu-scaling \
  --service-namespace ecs \
  --resource-id service/codexa-cluster/codexa-backend-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration '{
    "TargetValue": 70.0,
    "PredefinedMetricSpecification": {
      "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
    },
    "ScaleInCooldown": 300,
    "ScaleOutCooldown": 60
  }'
```

---

## 5. Deployment & Rolling Update CLI Commands

### Register New Task Definition Revision:
```bash
aws ecs register-task-definition --cli-input-json file://task-definition.json
```

### Trigger Zero-Downtime Service Update:
```bash
aws ecs update-service \
  --cluster codexa-cluster \
  --service codexa-backend-service \
  --task-definition codexa-backend-prod \
  --force-new-deployment
```

### Monitor Rollout Status:
```bash
aws ecs wait services-stable \
  --cluster codexa-cluster \
  --services codexa-backend-service
```
