@echo off
chcp 65001 >nul
title Atualizacao de Drivers
setlocal

:: ---- Exige administrador ----
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo Solicitando permissao de administrador...
    powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

echo ============================================
echo   ATUALIZACAO DE DRIVERS (Windows Update)
echo ============================================
echo.

:: ---- 1. Ponto de restauracao ----
echo [1/4] Criando ponto de restauracao...
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Enable-ComputerRestore -Drive $env:SystemDrive; Checkpoint-Computer -Description 'Antes da atualizacao de drivers' -RestorePointType MODIFY_SETTINGS -ErrorAction Stop; Write-Host 'Ponto criado.' } catch { Write-Host 'Nao foi possivel criar (pode ja existir um nas ultimas 24h).' }"
echo.

:: ---- 2. Reescanear dispositivos ----
echo [2/4] Reescaneando dispositivos...
pnputil /scan-devices
echo.

:: ---- 3. Buscar e instalar drivers via Windows Update ----
echo [3/4] Buscando e instalando drivers no Windows Update...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Set-ExecutionPolicy -Scope Process Bypass -Force;" ^
  "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;" ^
  "if (-not (Get-Module -ListAvailable PSWindowsUpdate)) { Install-PackageProvider -Name NuGet -Force | Out-Null; Install-Module PSWindowsUpdate -Force -Scope AllUsers };" ^
  "Import-Module PSWindowsUpdate;" ^
  "Get-WindowsUpdate -MicrosoftUpdate -UpdateType Driver -AcceptAll -Install -IgnoreReboot -Verbose"
echo.

:: ---- 4. (Opcional) instalar drivers de uma pasta local ----
:: Coloque arquivos .inf em uma pasta chamada "drivers" ao lado deste .cmd
if exist "%~dp0drivers" (
    echo [4/4] Instalando drivers da pasta local "drivers"...
    pnputil /add-driver "%~dp0drivers\*.inf" /subdirs /install
) else (
    echo [4/4] Pasta "drivers" nao encontrada - etapa ignorada.
)

echo.
echo ============================================
echo   Concluido. Reinicie o computador se solicitado.
echo ============================================
choice /c SN /m "Reiniciar agora"
if %errorlevel%==1 shutdown /r /t 10
endlocal
