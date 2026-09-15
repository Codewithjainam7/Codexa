# Codexa AWS ECS Fargate Deployment Guide

## Architecture Overview
Running Codexa on AWS ECS Fargate offers a serverless container architecture with zero EC2 host management overhead.

## Infrastructure Components
- **Application Load Balancer (ALB)**: Listens on HTTPS (443) using AWS Certificate Manager (ACM) SSL certificates.
- **ECS Fargate Task**: 2 vCPU, 4 GB RAM running the unified Codexa image.
- **Amazon Aurora PostgreSQL Serverless v2**: Relational datastore for persistent findings and scan histories.
- **Amazon EFS (Elastic File System)**: Shared POSIX volume mounted at `/var/codexa/data` for persistent rule caching.
- **AWS Secrets Manager**: Secure injection of `OPENROUTER_API_KEY` and database credentials at container boot.
