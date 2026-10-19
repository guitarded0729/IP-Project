@echo off
REM ====================================================================
REM Smart Campus Lost & Found Management System - Windows Startup Script
REM ====================================================================

echo [1/3] Preparing output directory...
if not exist out mkdir out

echo [2/3] Compiling Java backend sources...
javac -encoding UTF-8 -d out backend\src\exception\*.java backend\src\model\*.java backend\src\service\*.java backend\src\util\*.java backend\src\handler\*.java backend\src\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please ensure JDK 11+ is installed and on your PATH.
    pause
    exit /b %ERRORLEVEL%
)

echo [3/3] Starting Smart Campus Lost ^& Found Web Server on port 8080...
echo Open your browser at: http://localhost:8080
java -cp out Main
pause
