@echo off
title MedPlus - Stop All Services
echo ===================================================
echo             Stopping MedPlus Services             
echo ===================================================
echo.

echo 1. Terminating any running Node.js processes...
taskkill /f /im node.exe >nul 2>nul

echo 2. Freeing port 5000 (Backend API)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5000') do (
    taskkill /f /pid %%a >nul 2>nul
)

echo 3. Freeing port 5173 (Vite Dev Server)...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173') do (
    taskkill /f /pid %%a >nul 2>nul
)

echo.
echo ===================================================
echo Clean up complete! All services stopped and ports freed.
echo You can now safely run start-all.bat to restart.
echo ===================================================
echo.
pause
