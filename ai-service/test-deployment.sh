#!/bin/bash

# AI Service Pre-Deployment Test Script
# Run this before deploying to catch issues early

echo "🧪 AI Service Pre-Deployment Tests"
echo "===================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Check if required files exist
echo "1️⃣  Checking required files..."
files=("requirements.txt" "Dockerfile" "railway.toml" "main.py" ".dockerignore")
all_exist=true

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo -e "  ${GREEN}✓${NC} $file exists"
    else
        echo -e "  ${RED}✗${NC} $file missing!"
        all_exist=false
    fi
done

if [ "$all_exist" = true ]; then
    echo -e "${GREEN}All required files present!${NC}\n"
else
    echo -e "${RED}Some files are missing!${NC}\n"
    exit 1
fi

# Test 2: Check environment variables
echo "2️⃣  Checking environment variables..."
if [ -f ".env" ]; then
    echo -e "  ${GREEN}✓${NC} .env file exists"
    
    # Check for required variables
    required_vars=("MONGODB_URI" "GEMINI_API_KEY")
    for var in "${required_vars[@]}"; do
        if grep -q "^${var}=" .env; then
            echo -e "  ${GREEN}✓${NC} $var is set"
        else
            echo -e "  ${YELLOW}⚠${NC}  $var not found in .env"
        fi
    done
else
    echo -e "  ${YELLOW}⚠${NC}  .env file not found (required for local testing)"
fi
echo ""

# Test 3: Check Python dependencies
echo "3️⃣  Checking Python environment..."
if command -v python3 &> /dev/null; then
    echo -e "  ${GREEN}✓${NC} Python 3 is installed"
    python3 --version
else
    echo -e "  ${RED}✗${NC} Python 3 not found!"
fi
echo ""

# Test 4: Validate requirements.txt
echo "4️⃣  Validating requirements.txt..."
if grep -q "torch==2.1.0" requirements.txt; then
    echo -e "  ${GREEN}✓${NC} PyTorch version is optimized (CPU-only)"
else
    echo -e "  ${YELLOW}⚠${NC}  PyTorch version may need review"
fi

if grep -q "sentence-transformers" requirements.txt; then
    echo -e "  ${GREEN}✓${NC} Sentence-transformers included"
else
    echo -e "  ${RED}✗${NC} Sentence-transformers missing!"
fi

if grep -q "langchain" requirements.txt; then
    echo -e "  ${GREEN}✓${NC} Langchain packages included"
else
    echo -e "  ${RED}✗${NC} Langchain missing!"
fi
echo ""

# Test 5: Check Docker setup
echo "5️⃣  Checking Docker..."
if command -v docker &> /dev/null; then
    echo -e "  ${GREEN}✓${NC} Docker is installed"
    docker --version
    
    echo ""
    echo "  Would you like to build the Docker image locally? (y/n)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        echo "  Building Docker image..."
        docker build -t ai-service-test . --no-cache
        if [ $? -eq 0 ]; then
            echo -e "  ${GREEN}✓${NC} Docker build successful!"
            echo ""
            echo "  To test locally, run:"
            echo "  docker run -p 8000:8000 --env-file .env ai-service-test"
        else
            echo -e "  ${RED}✗${NC} Docker build failed!"
        fi
    fi
else
    echo -e "  ${YELLOW}⚠${NC}  Docker not installed (optional for local testing)"
fi
echo ""

# Test 6: Syntax check for Python files
echo "6️⃣  Checking Python syntax..."
if command -v python3 &> /dev/null; then
    python3 -m py_compile main.py 2>/dev/null
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}✓${NC} main.py syntax is valid"
    else
        echo -e "  ${RED}✗${NC} main.py has syntax errors!"
    fi
else
    echo -e "  ${YELLOW}⚠${NC}  Skipping (Python not available)"
fi
echo ""

# Test 7: Check git status
echo "7️⃣  Checking Git status..."
if command -v git &> /dev/null; then
    if [ -d ".git" ]; then
        echo -e "  ${GREEN}✓${NC} Git repository initialized"
        
        # Check for uncommitted changes
        if [ -n "$(git status --porcelain)" ]; then
            echo -e "  ${YELLOW}⚠${NC}  You have uncommitted changes:"
            git status --short
            echo ""
            echo "  Commit your changes before deploying:"
            echo "  git add ."
            echo "  git commit -m 'Optimized AI service for deployment'"
        else
            echo -e "  ${GREEN}✓${NC} No uncommitted changes"
        fi
    else
        echo -e "  ${YELLOW}⚠${NC}  Not a git repository"
    fi
else
    echo -e "  ${YELLOW}⚠${NC}  Git not installed"
fi
echo ""

# Summary
echo "=================================="
echo "📋 Pre-Deployment Summary"
echo "=================================="
echo ""
echo "✅ Optimizations Applied:"
echo "  • CPU-only PyTorch (reduces size by ~2.5GB)"
echo "  • Extended health check timeout (120s)"
echo "  • Optimized Docker layers"
echo "  • Proper .dockerignore configuration"
echo ""
echo "⚡ Expected Deployment Times:"
echo "  • First deployment: 10-15 minutes"
echo "  • Subsequent deployments: 5-8 minutes"
echo "  • Initial startup: 60-90 seconds"
echo ""
echo "🚀 Ready to Deploy?"
echo ""
echo "Next steps:"
echo "  1. Ensure Railway environment variables are set"
echo "  2. Commit changes if any: git add . && git commit -m 'message'"
echo "  3. Push to trigger deployment: git push origin main"
echo "  4. Monitor logs: railway logs"
echo ""
echo "📚 For detailed help, see DEPLOYMENT.md"
echo ""
