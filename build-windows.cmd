@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-windows.ps1" %*
if errorlevel 1 (
    echo LINAS build failed.
    pause
    exit /b 1
)
pause
