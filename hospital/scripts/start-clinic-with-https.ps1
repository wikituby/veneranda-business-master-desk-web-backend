# Start Quarkus with LAN HTTPS (:8443) for multi-PC access from venerandahospital.org
$ErrorActionPreference = 'Stop'
$backend = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $backend
if (-not (Test-Path 'certs\clinic-server.p12')) {
  powershell -NoProfile -File (Join-Path $PSScriptRoot 'generate-clinic-tls.ps1')
}
$env:QUARKUS_PROFILE = if ($env:QUARKUS_PROFILE) { "$($env:QUARKUS_PROFILE),lan" } else { 'lan' }
Write-Host "QUARKUS_PROFILE=$($env:QUARKUS_PROFILE)"
if (Test-Path '.\mvnw.cmd') { .\mvnw.cmd quarkus:dev }
else { mvn quarkus:dev }