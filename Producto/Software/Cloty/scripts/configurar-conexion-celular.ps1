# Configura la PC para que el celular alcance cloty-api (puerto 8080).
# Uso:
#   .\configurar-conexion-celular.ps1 -Modo wifi
#   .\configurar-conexion-celular.ps1 -Modo usb
# Requiere ejecutar PowerShell como Administrador para reglas de firewall.

param(
    [ValidateSet("wifi", "usb")]
    [string]$Modo = "wifi"
)

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$localProps = Join-Path $root "local.properties"
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"

function Set-ClotyApiHost([string]$hostIp) {
    $lines = @()
    if (Test-Path $localProps) {
        $lines = Get-Content $localProps | Where-Object {
            $_ -notmatch '^\s*cloty\.api\.(host|port)\s*='
        }
    }
    $header = @(
        "# IP para apps Android (recompilar tras cambiar).",
        "# wifi: IPv4 de esta PC (ipconfig). usb: 127.0.0.1 + adb reverse.",
        "cloty.api.host=$hostIp",
        "cloty.api.port=8080"
    )
    ($lines + $header) | Set-Content -Encoding UTF8 $localProps
    Write-Host "Actualizado $localProps -> cloty.api.host=$hostIp"
}

Write-Host "=== Firewall: permitir TCP 8080 (todas las redes) ===" -ForegroundColor Cyan
netsh advfirewall firewall delete rule name="Cloty API 8080" 2>$null | Out-Null
netsh advfirewall firewall add rule name="Cloty API 8080" dir=in action=allow protocol=TCP localport=8080 profile=any | Out-Null

try {
    Set-NetConnectionProfile -InterfaceAlias "Wi-Fi" -NetworkCategory Private -ErrorAction Stop
    Write-Host "Red Wi-Fi marcada como Privada (recomendado para desarrollo)."
} catch {
    Write-Warning "No se pudo cambiar el perfil de red. Hazlo manualmente en Configuración de Windows."
}

if ($Modo -eq "usb") {
    if (-not (Test-Path $adb)) {
        throw "No se encontró adb en $adb"
    }
    & $adb reverse tcp:8080 tcp:8080
    Write-Host "adb reverse activo: celular 127.0.0.1:8080 -> PC localhost:8080" -ForegroundColor Green
    Set-ClotyApiHost "127.0.0.1"
} else {
    $ipv4 = (Get-NetIPAddress -AddressFamily IPv4 |
        Where-Object { $_.IPAddress -match '^192\.168\.' -and $_.PrefixOrigin -ne 'WellKnown' } |
        Select-Object -First 1).IPAddress
    if (-not $ipv4) {
        $ipv4 = (Get-NetIPAddress -AddressFamily IPv4 |
            Where-Object { $_.IPAddress -notlike '127.*' } |
            Select-Object -First 1).IPAddress
    }
    if (-not $ipv4) { throw "No se detectó IPv4 LAN. Configura cloty.api.host manualmente." }
    Set-ClotyApiHost $ipv4
    Write-Host ""
    Write-Host "Si el celular sigue sin conectar por Wi-Fi:" -ForegroundColor Yellow
    Write-Host "  1. En el router, desactiva 'Aislamiento AP' / 'Client isolation'."
    Write-Host "  2. Celular y PC en la misma red (no invitados)."
    Write-Host "  3. O usa: .\configurar-conexion-celular.ps1 -Modo usb (USB + depuración)"
}

Write-Host ""
Write-Host "Recompila e instala la app:" -ForegroundColor Cyan
Write-Host "  cd clotyadministrador"
Write-Host "  .\gradlew.bat :app:installDebug"
