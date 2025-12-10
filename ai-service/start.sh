#!/bin/bash
# Railway AI Service Start Script

echo "🚀 Starting Smart Hire AI Service..."

# Set environment variables
export PYTHONUNBUFFERED=1
export PORT=${PORT:-5001}

# Create model cache directory
mkdir -p model_cache
export HF_HOME=./model_cache
export TRANSFORMERS_CACHE=./model_cache
export SENTENCE_TRANSFORMERS_HOME=./model_cache

echo "📦 Installing dependencies..."
pip install --no-cache-dir -r requirements.txt

echo "✅ Starting FastAPI server..."
uvicorn main:app --host 0.0.0.0 --port $PORT
