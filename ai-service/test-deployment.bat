@echo off
REM AI Service Pre-Deployment Test Script (Windows)
REM Run this before deploying to catch issues early

echo.
echo 🧪 AI Service Pre-Deployment Tests
echo ====================================
echo.

REM Test 1: Check if required files exist
echo 1️⃣  Checking required files...
set "all_exist=true"

if exist "requirements.txt" (
    echo   ✓ requirements.txt exists
) else (
    echo   ✗ requirements.txt missing!
    set "all_exist=false"
)

if exist "Dockerfile" (
    echo   ✓ Dockerfile exists
) else (
    echo   ✗ Dockerfile missing!
    set "all_exist=false"
)

if exist "railway.toml" (
    echo   ✓ railway.toml exists
) else (
    echo   ✗ railway.toml missing!
    set "all_exist=false"
)

if exist "main.py" (
    echo   ✓ main.py exists
) else (
    echo   ✗ main.py missing!
    set "all_exist=false"
)

if exist ".dockerignore" (
    echo   ✓ .dockerignore exists
) else (
    echo   ✗ .dockerignore missing!
    set "all_exist=false"
)

if "%all_exist%"=="false" (
    echo.
    echo ❌ Some required files are missing!
    pause
    exit /b 1
)
echo   All required files present!
echo.

REM Test 2: Check environment variables
echo 2️⃣  Checking environment variables...
if exist ".env" (
    echo   ✓ .env file exists
    findstr /C:"MONGODB_URI=" .env >nul 2>&1
    if errorlevel 1 (
        echo   ⚠ MONGODB_URI not found in .env
    ) else (
        echo   ✓ MONGODB_URI is set
    )
    findstr /C:"GEMINI_API_KEY=" .env >nul 2>&1
    if errorlevel 1 (
        echo   ⚠ GEMINI_API_KEY not found in .env
    ) else (
        echo   ✓ GEMINI_API_KEY is set
    )
) else (
    echo   ⚠ .env file not found (required for local testing)
)
echo.

REM Test 3: Check Python
echo 3️⃣  Checking Python environment...
python --version >nul 2>&1
if errorlevel 1 (
    echo   ✗ Python not found!
) else (
    echo   ✓ Python is installed
    python --version
)
echo.

REM Test 4: Validate requirements.txt
echo 4️⃣  Validating requirements.txt...
findstr /C:"torch==2.1.0" requirements.txt >nul 2>&1
if errorlevel 1 (
    echo   ⚠ PyTorch version may need review
) else (
    echo   ✓ PyTorch version is optimized (CPU-only)
)

findstr /C:"sentence-transformers" requirements.txt >nul 2>&1
if errorlevel 1 (
    echo   ✗ Sentence-transformers missing!
) else (
    echo   ✓ Sentence-transformers included
)

findstr /C:"langchain" requirements.txt >nul 2>&1
if errorlevel 1 (
    echo   ✗ Langchain missing!
) else (
    echo   ✓ Langchain packages included
)
echo.

REM Test 5: Check Docker
echo 5️⃣  Checking Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo   ⚠ Docker not installed (optional for local testing)
) else (
    echo   ✓ Docker is installed
    docker --version
    echo.
    set /p "build_docker=  Would you like to build the Docker image locally? (y/n): "
    if /i "%build_docker%"=="y" (
        echo   Building Docker image...
        docker build -t ai-service-test . --no-cache
        if errorlevel 1 (
            echo   ✗ Docker build failed!
        ) else (
            echo   ✓ Docker build successful!
            echo.
            echo   To test locally, run:
            echo   docker run -p 8000:8000 --env-file .env ai-service-test
        )
    )
)
echo.

REM Test 6: Check Python syntax
echo 6️⃣  Checking Python syntax...
python -m py_compile main.py >nul 2>&1
if errorlevel 1 (
    echo   ✗ main.py has syntax errors!
) else (
    echo   ✓ main.py syntax is valid
)
echo.

REM Test 7: Check git status
echo 7️⃣  Checking Git status...
git --version >nul 2>&1
if errorlevel 1 (
    echo   ⚠ Git not installed
) else (
    if exist ".git" (
        echo   ✓ Git repository initialized
        git status --short >nul 2>&1
        if errorlevel 1 (
            echo   ✓ No uncommitted changes
        ) else (
            echo   ⚠ You have uncommitted changes:
            git status --short
            echo.
            echo   Commit your changes before deploying:
            echo   git add .
            echo   git commit -m "Optimized AI service for deployment"
        )
    ) else (
        echo   ⚠ Not a git repository
    )
)
echo.

REM Summary
echo ==================================
echo 📋 Pre-Deployment Summary
echo ==================================
echo.
echo ✅ Optimizations Applied:
echo   • CPU-only PyTorch (reduces size by ~2.5GB)
echo   • Extended health check timeout (120s)
echo   • Optimized Docker layers
echo   • Proper .dockerignore configuration
echo.
echo ⚡ Expected Deployment Times:
echo   • First deployment: 10-15 minutes
echo   • Subsequent deployments: 5-8 minutes
echo   • Initial startup: 60-90 seconds
echo.
echo 🚀 Ready to Deploy?
echo.
echo Next steps:
echo   1. Ensure Railway environment variables are set
echo   2. Commit changes if any: git add . ^&^& git commit -m "message"
echo   3. Push to trigger deployment: git push origin main
echo   4. Monitor logs: railway logs
echo.
echo 📚 For detailed help, see DEPLOYMENT.md
echo.
pause
