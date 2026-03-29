$ErrorActionPreference = 'SilentlyContinue'
$response = Invoke-WebRequest -Uri 'http://localhost:9001/exchanges'
$response.Content
