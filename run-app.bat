@echo off
title MedPlus - Deploy and Run App
echo ===================================================
echo   MedPlus - Build, Install, and Run on Device
echo ===================================================
echo.

:: 1. Locate ADB
where adb >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set ADB_CMD=adb
    goto CHECK_DEVICE
)

if exist "C:\Users\kadha\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
    set ADB_CMD="C:\Users\kadha\AppData\Local\Android\Sdk\platform-tools\adb.exe"
    goto CHECK_DEVICE
)

if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set ADB_CMD="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
    goto CHECK_DEVICE
)

echo [ERROR] adb.exe could not be located.
echo Please ensure Android SDK is installed.
pause
exit

:CHECK_DEVICE
echo Found ADB: %ADB_CMD%
echo Checking for connected devices...
%ADB_CMD% devices

echo.
echo Building and installing debug APK on your phone...
echo (This may take a minute on the first run)
echo.

call .\gradlew.bat installDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] App installed successfully!
    echo Starting the app on your phone...
    %ADB_CMD% shell am start -n com.example.medplus/.MainActivity
) else (
    echo.
    echo [ERROR] Failed to compile or install the app.
    echo Please make sure your phone is connected and USB Debugging is enabled.
)

echo.
pause
