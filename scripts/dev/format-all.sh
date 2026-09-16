#!/usr/bin/env bash
cd frontend && npm run format
cd ../backend && mvn spotless:apply || true
