@echo off
cls
echo ============================================
echo   SmartHire Backend - Starting...
echo ============================================
echo.

cd /d "%~dp0"

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: Maven is not installed!
    echo.
    echo ============================================
    echo   QUICK FIX OPTIONS:
    echo ============================================
    echo.
    echo   🎯 RECOMMENDED: Use IntelliJ IDEA ^(FREE^)
    echo      → Download: https://www.jetbrains.com/idea/download/
    echo      → Open backend folder in IntelliJ
    echo      → Right-click SmartHireApplication.java and Run
    echo      → NO Maven installation needed!
    echo.
    echo   🔧 Option 2: Install Maven
    echo      → With Chocolatey: choco install maven
    echo      → Manual: https://maven.apache.org/download.cgi
    echo.
    echo   📖 Option 3: Use VS Code or Eclipse
    echo      → Both handle Maven automatically
    echo.
    echo ============================================
    echo.
    echo   📄 See full guide: MAVEN_NOT_INSTALLED_FIX.md
    echo.
    pause
    
    REM Open the guide
    start "" "MAVEN_NOT_INSTALLED_FIX.md"
    exit /b 1
)

echo ✅ Maven found!
echo.

REM Check Java
echo Checking Java installation...
java -version 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: Java is not installed!
    echo Please install Java 17 or higher
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)
echo ✅ Java installed
echo.

echo ============================================
echo   Starting Spring Boot Application...
echo ============================================
echo.
echo   Backend URL: http://localhost:8080
echo   Health Check: http://localhost:8080/api/health
echo.
echo   Press Ctrl+C to stop
echo ============================================
echo.

mvn spring-boot:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Failed to start backend!
    echo.
    echo Check the error message above for details.
    echo.
    pause
)
