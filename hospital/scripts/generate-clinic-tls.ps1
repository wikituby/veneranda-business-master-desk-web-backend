# Generate clinic TLS keystore for Quarkus (:8443) + clinic-ca.cer for other PCs to trust.
param(
  [string[]]$ExtraIps = @('192.168.1.130')
)

$ErrorActionPreference = 'Stop'
$backend = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $backend 'pom.xml'))) {
  $backend = 'D:\from recent\hospitaldesktop\development\backend\hospital\hospital'
}
$certDir = Join-Path $backend 'certs'
New-Item -ItemType Directory -Path $certDir -Force | Out-Null
$pass = 'clinic-tls-changeit'
$securePass = ConvertTo-SecureString -String $pass -Force -AsPlainText

$ips = @(
  '127.0.0.1',
  '192.168.1.114',
  '192.168.1.130',
  '192.168.1.187'
) + @($ExtraIps)
$ips = $ips | Select-Object -Unique
$lan = @(Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
  Where-Object { $_.IPAddress -match '^192\.168\.' -or $_.IPAddress -match '^10\.' } |
  Select-Object -ExpandProperty IPAddress)
$ips = @($ips + $lan) | Select-Object -Unique

$parts = @('DNS=localhost') + @($ips | ForEach-Object { "IPAddress=$_" })
$sanText = $parts -join '&'

$cert = New-SelfSignedCertificate `
  -Subject 'CN=Veneranda Clinic API' `
  -CertStoreLocation 'Cert:\CurrentUser\My' `
  -KeyExportPolicy Exportable `
  -KeySpec KeyExchange `
  -KeyLength 2048 `
  -HashAlgorithm SHA256 `
  -NotAfter (Get-Date).AddYears(10) `
  -TextExtension @("2.5.29.17={text}$sanText")

$pfx = Join-Path $certDir 'clinic-server.p12'
$cer = Join-Path $certDir 'clinic-ca.cer'
Export-PfxCertificate -Cert $cert -FilePath $pfx -Password $securePass | Out-Null
Export-Certificate -Cert $cert -FilePath $cer -Type CERT | Out-Null
Set-Content -LiteralPath (Join-Path $certDir 'clinic-server.pass') -Value $pass -NoNewline -Encoding ascii

# Also copy CA next to frontend trust script
$feCer = 'D:\from recent\hospitaldesktop\development\frontend_veneranda\ngwiki\scripts\clinic-ca.cer'
Copy-Item -LiteralPath $cer -Destination $feCer -Force

Write-Host "Wrote $pfx"
Write-Host "Wrote $cer"
Write-Host "SANs: $sanText"
Write-Host 'Restart Quarkus on the clinic server so :8443 is active.'
Write-Host 'On each clinic PC run: scripts/trust-clinic-https.ps1'