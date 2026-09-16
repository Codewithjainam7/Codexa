#!/usr/bin/env bash
curl -X POST -F "file=@code.zip" http://localhost:8080/api/v1/analysis/upload-zip
