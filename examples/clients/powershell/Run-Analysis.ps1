param([string]$FilePath)
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/analysis/upload-zip" -InFile $FilePath
