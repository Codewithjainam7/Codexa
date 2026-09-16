Write-Host "Running Codexa local benchmark..."
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/analysis/benchmark"
