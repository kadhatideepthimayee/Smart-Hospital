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

echo Starting Backend API Server...
start "MedPlus Backend Server" cmd /k "cd /d backend && node server.js"

echo Starting Vite Web Client...
start "MedPlus Web Client" cmd /k "cd /d MedPlus-Web && npm run dev"

echo Starting Android ADB Port Forwarding Watcher...
start "MedPlus ADB Watcher" cmd /k "run-adb-reverse-watcher.bat"

echo.
echo ===================================================
echo All services started in separate terminal windows!
echo Web Portal: http://127.0.0.1:5173/
echo Backend API: http://127.0.0.1:5000/
echo You can now close this control panel window.
echo ===================================================
echo.
pause
