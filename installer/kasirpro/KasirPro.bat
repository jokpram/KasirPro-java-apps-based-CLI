@echo off
title KASIR PRO - Sistem Kasir Modern
echo.
echo ================================================
echo        KASIR PRO - Sistem Kasir Modern
echo ================================================
echo.

:: Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java tidak ditemukan!
    echo.
    echo Silakan install Java 21 atau lebih tinggi dari:
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)

:: Check Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
echo Java Version: %JAVA_VERSION%
echo.

:: Run the application
echo Menjalankan KASIR PRO...
echo.
java -jar "%~dp0kasirpro-1.0.0-all.jar"

pause
