@echo off
REM Prueba correo en AWS tras login superadmin. Uso:
REM   scripts\aws\probar-correo-aws.bat tu@gmail.com

set HOST=18.220.5.103
set EMAIL=%1
if "%EMAIL%"=="" (
  echo Indique email destino: scripts\aws\probar-correo-aws.bat tu@gmail.com
  exit /b 1
)

for /f "delims=" %%i in ('curl.exe -s -X POST http://%HOST%:8080/api/auth/login -H "Content-Type: application/json" -d "{\"identificador\":\"superadmin\",\"password\":\"super123\"}"') do set LOGIN=%%i

powershell -NoProfile -Command "$j = '%LOGIN%' | ConvertFrom-Json; $h = @{ Authorization = 'Bearer ' + $j.token }; Write-Host '--- Estado correo ---'; Invoke-RestMethod -Uri 'http://%HOST%:8080/api/sistema/correo/estado' -Headers $h | ConvertTo-Json; Write-Host '--- Prueba envio ---'; Invoke-RestMethod -Uri 'http://%HOST%:8080/api/sistema/correo/probar' -Method POST -Headers $h -ContentType 'application/json' -Body ('{\"email\":\"%EMAIL%\"}') | ConvertTo-Json"
