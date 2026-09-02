@echo off
echo ===================================================
echo   COMPILING SMART BANK ENTERPRISE MANAGEMENT SYSTEM
echo ===================================================

if not exist "build" mkdir "build"
powershell -Command "javac -encoding UTF-8 -d build (Get-ChildItem -Path src -Recurse -Filter *.java).FullName"

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation finished with 0 errors!
) else (
    echo [ERROR] Compilation failed.
)
pause
