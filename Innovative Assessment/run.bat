@echo off
echo ===================================================
echo   LAUNCHING SMART BANK ENTERPRISE MANAGEMENT SYSTEM
echo ===================================================

if not exist "build" (
    echo Build directory not found. Compiling first...
    call build.bat
)

java -cp build com.smartbank.Main
