@echo off
title MedPlus ADB Reverse Watcher
echo ===================================================
echo   MedPlus - Background ADB Reverse Watcher Active   
echo ===================================================
echo.
echo Watching for connected Android devices to map port 5000...
echo Keep this window minimized in the background.
echo.

:: Locate ADB
if exist "C:\Users\kadha\AppData\Local\Android\Sdk\platform-tools\adb.exe" (
    set ADB_CMD="C:\Users\kadha\AppData\Local\Android\Sdk\platform-tools\adb.exe"
) else if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set ADB_CMD="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
) else (
    set ADB_CMD=adb
)

:LOOP
:: Run adb reverse to map port 5000 from phone to PC
%ADB_CMD% reverse tcp:5000 tcp:5000 >nul 2>nul
:: Sleep for 5 seconds using ping (works in non-interactive background tasks)
ping -n 6 127.0.0.1 >nul
goto LOOP
