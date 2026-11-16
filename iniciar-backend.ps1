$ErrorActionPreference = 'Stop'
$local = Join-Path $PSScriptRoot '.local'
New-Item -ItemType Directory -Force -Path $local | Out-Null
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
if (-not $env:JWT_SECRET) {
    $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
}
$rng.GetBytes($bytes)
$env:TLS_KEYSTORE_PASSWORD = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$keystore = Join-Path $local 'localhost.p12'
if (Test-Path -LiteralPath $keystore) {
    Remove-Item -LiteralPath $keystore
}
$keytool = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/keytool.exe' } else { 'keytool' }
& $keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore $keystore -storepass:env TLS_KEYSTORE_PASSWORD -validity 30 -dname 'CN=localhost' -ext 'SAN=dns:localhost,ip:127.0.0.1' -noprompt
if ($LASTEXITCODE -ne 0) { throw 'No se pudo generar el certificado local.' }
$env:TLS_KEYSTORE = ([System.Uri]$keystore).AbsoluteUri
Push-Location (Join-Path $PSScriptRoot 'backend')
try {
    & .\mvnw.cmd spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar el backend.' }
} finally {
    Pop-Location
}
