@echo off
title MedPlus - All Services Control Panel
echo ===================================================
echo             MedPlus Starter Panel                 
echo ===================================================
echo.
echo This script will start all required servers for MedPlus:
echo 1. Backend API Server (Port 5000)
echo 2. React/Vite Frontend Web Server (Port 5173)
echo 3. Android Port Forwarding Watcher (for physical devices)
echo.
echo NOTE: If you encounter network errors, open the web client
echo in your browser using: http://127.0.0.1:5173/ instead of localhost.
echo.
echo Keep the launched terminal windows open to keep the servers active.
echo.
pause

echo Starting Vite Web Client...
start "MedPlus Web Client" cmd /k "cd /d MedPlus-Web && npm run dev"

echo.
echo ===================================================
echo Vite Web Client started in a separate window!
echo Web Portal: http://127.0.0.1:5173/
echo You can now close this window.
echo ===================================================
echo.
pause
