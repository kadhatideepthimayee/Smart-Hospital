@echo off
title MedPlus ADB Port Forwarder
echo ===================================================
echo   MedPlus - Setup USB Connection to Local Server   
echo ===================================================
echo.

:: 1. Try standard adb on PATH
where adb >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set ADB_CMD=adb
    goto RUN_ADB
)

:: 2. Try default Windows SDK installation paths
if exist "C:\Users\kadha\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
    set ADB_CMD="C:\Users\kadha\AppData\Local\Android\Sdk\platform-tools\adb.exe"
    goto RUN_ADB
)

if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set ADB_CMD="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
    goto RUN_ADB
)

echo [ERROR] adb.exe could not be located on your computer automatically.
echo Please make sure Android Studio / SDK is installed.
echo.
pause
exit

:RUN_ADB
echo Found ADB command: %ADB_CMD%
echo Forwarding port 5000 from phone to PC...
%ADB_CMD% reverse tcp:5000 tcp:5000

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] USB Port Forwarding setup successfully!
    echo.
    echo Keep this window open or you can close it. Your phone will now
    echo be able to connect to the backend server over the USB cable
    echo without needing Wi-Fi connection or getting blocked by Wi-Fi security.
) else (
    echo.
    echo [ERROR] Failed to setup port forwarding.
    echo Please check:
    echo   1. Your phone is connected to the PC via USB cable.
    echo   2. "USB Debugging" is enabled in Developer Options on your phone.
)
echo.
pause
