Remove-Item -Path "$env:TEMP\codexa-*" -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "Temporary analysis caches cleaned."
