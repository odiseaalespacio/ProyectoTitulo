# Libera bloqueos de Gradle en proyectos Android (Windows + OneDrive)
param(
    [ValidateSet("clotyadministrador", "cloty_colegio", "cloty_apoderado", "all")]
    [string]$Project = "all"
)

$root = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $root "clotyadministrador"))) {
    $root = "C:\Users\TIMI\OneDrive\Desktop\Cloty"
}

$projects = switch ($Project) {
    "all" { @("clotyadministrador", "cloty_colegio", "cloty_apoderado") }
    default { @($Project) }
}

Write-Host "Deteniendo daemons de Gradle..." -ForegroundColor Cyan
foreach ($p in $projects) {
    $dir = Join-Path $root $p
    if (Test-Path (Join-Path $dir "gradlew.bat")) {
        Push-Location $dir
        & .\gradlew.bat --stop 2>$null
        Pop-Location
    }
}
Start-Sleep -Seconds 3

foreach ($p in $projects) {
    $buildDir = Join-Path $root "$p\app\build"
    if (Test-Path $buildDir) {
        Write-Host "Eliminando $buildDir ..." -ForegroundColor Yellow
        Remove-Item -Path $buildDir -Recurse -Force -ErrorAction SilentlyContinue
        if (Test-Path $buildDir) {
            Write-Host "  No se pudo borrar todo. Cierre Android Studio y pause OneDrive, luego ejecute de nuevo." -ForegroundColor Red
        } else {
            Write-Host "  OK" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "Listo. Compile desde Android Studio o con: .\gradlew.bat assembleDebug" -ForegroundColor Green
