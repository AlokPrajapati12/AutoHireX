# 🚂 Railway Deployment Guide - AutoHireX

## 📋 Prerequisites
- Railway account (sign up at https://railway.app)
- GitHub account (to connect repository)
- MongoDB Atlas connection string
- Google API Key for Gemini

---

## 🎯 Deployment Order

```
1. AI Service (FastAPI) → Get AI_SERVICE_URL
2. Frontend (Angular) → Get FRONTEND_URL  
3. Update Backend (Render) → Add new URLs
```

---

## 🤖 PART 1: Deploy AI Service (FastAPI)

### Step 1: Prepare Your Code

✅ **Files Created:**
- `ai-service/Dockerfile` - Container configuration
- `ai-service/.dockerignore` - Exclude unnecessary files
- `ai-service/railway.json` - Railway configuration

### Step 2: Push to GitHub (if not already)

```bash
cd "E:\New Data\AutoHireX"

# Initialize git if needed
git init
git add .
git commit -m "Add Railway deployment configs"

# Push to GitHub
git remote add origin https://github.com/YOUR_USERNAME/AutoHireX.git
git push -u origin main
```

### Step 3: Deploy on Railway

1. **Go to Railway Dashboard:**
   - Visit: https://railway.app/dashboard
   - Click "New Project"

2. **Connect GitHub Repository:**
   - Choose "Deploy from GitHub repo"
   - Select your `AutoHireX` repository
   - Click "Deploy Now"

3. **Configure Service:**
   - Railway will auto-detect multiple services
   - Select `ai-service` folder for deployment
   - Railway will detect the Dockerfile

4. **Set Environment Variables:**
   
   Go to your AI Service → Variables → Add these:

   ```bash
   # Required Variables
   GOOGLE_API_KEY=your_google_gemini_api_key
   MONGODB_URI=your_mongodb_atlas_connection_string
   PORT=5001
   
   # Python Cache Settings (Already in Dockerfile, but can override)
   PYTHONUNBUFFERED=1
   HF_HOME=/app/model_cache
   ```

5. **Configure Deployment:**
   - Root Directory: `/ai-service`
   - Builder: Dockerfile
   - Port: 5001

6. **Deploy:**
   - Click "Deploy"
   - Wait 3-5 minutes for build

7. **Get Your AI Service URL:**
   - After deployment: Click on your service
   - Go to "Settings" → "Networking"
   - Copy your public URL (e.g., `https://ai-service-production-xxxx.up.railway.app`)
   - Test it: `https://your-ai-url/health`

---

## 🎨 PART 2: Deploy Frontend (Angular)

### Step 1: Update Production Environment

**Before deploying, update your URLs:**

Edit: `frontend/src/environments/environment.prod.ts`

```typescript
export const environment = {
  production: true,
  apiUrl: "https://smart-hire-backend.onrender.com/api",
  aiServiceUrl: "https://your-ai-service.up.railway.app"
};
```

Commit the changes:
```bash
git add .
git commit -m "Update production URLs"
git push
```

### Step 2: Deploy Frontend on Railway

1. **Create New Service:**
   - In Railway Dashboard, click "+ New"
   - Choose "GitHub Repo" again
   - Select `AutoHireX` repository

2. **Configure Frontend Service:**
   - Root Directory: `/frontend`
   - Builder: Nixpacks (auto-detected)
   - Build Command: `npm run build:railway`
   - Start Command: `npx serve dist/smart-hire-frontend/browser -s -l 3000`

3. **Set Environment Variables:**
   
   ```bash
   NODE_ENV=production
   PORT=3000
   ```

4. **Deploy:**
   - Click "Deploy"
   - Wait 3-5 minutes

5. **Get Your Frontend URL:**
   - Copy from Settings → Networking
   - Example: `https://frontend-production-xxxx.up.railway.app`

---

## 🔗 PART 3: Connect All Services

### Update Backend (Render)

Go to your Render Dashboard → Backend Service → Environment

**Add these new variables:**

```bash
# Frontend URL
FRONTEND_URL=https://your-frontend.up.railway.app

# AI Service URLs
AI_SERVICE_URL=https://your-ai-service.up.railway.app
FASTAPI_BASE_URL=https://your-ai-service.up.railway.app

# Update CORS in code (if needed)
ALLOWED_ORIGINS=http://localhost:4200,https://your-frontend.up.railway.app
```

### Update Backend CORS Configuration

If your Spring Boot backend has hardcoded CORS:

```java
// In WebConfig.java or SecurityConfig.java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(
            "http://localhost:4200",
            "https://your-frontend.up.railway.app"  // Add this
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
}
```

Redeploy backend after changes.

---

## ✅ Deployment Checklist

### AI Service ✅
- [ ] Dockerfile created
- [ ] Deployed on Railway
- [ ] GOOGLE_API_KEY configured
- [ ] MONGODB_URI configured
- [ ] Health endpoint responds: `/health`
- [ ] Test endpoint: `/api/parse-resume`
- [ ] URL saved for backend config

### Frontend ✅
- [ ] environment.prod.ts updated with backend URL
- [ ] environment.prod.ts updated with AI service URL
- [ ] Deployed on Railway
- [ ] Build successful (check logs)
- [ ] App loads in browser
- [ ] Can navigate routes
- [ ] URL saved for backend CORS

### Backend (Render) ✅
- [ ] FRONTEND_URL added to environment
- [ ] AI_SERVICE_URL added to environment
- [ ] FASTAPI_BASE_URL added to environment
- [ ] CORS updated with frontend URL
- [ ] Service redeployed
- [ ] All endpoints working

---

## 🧪 Testing Your Deployment

### 1. Test AI Service

```bash
# Health Check
curl https://your-ai-service.up.railway.app/health

# Should return: {"status": "healthy"}
```

### 2. Test Frontend

- Visit: `https://your-frontend.up.railway.app`
- Try logging in
- Check browser console for errors
- Verify API calls are going to correct backend

### 3. Test Full Integration

1. **Create Job Posting:** Frontend → Backend → MongoDB
2. **Parse Resume:** Frontend → Backend → AI Service
3. **AI Analysis:** Backend → AI Service → Response

---

## 🐛 Troubleshooting

### AI Service Issues

**Problem:** Build fails
```
Solution: Check Dockerfile syntax and requirements.txt
- Ensure all dependencies are listed
- Check Python version compatibility
```

**Problem:** Out of memory during build
```
Solution: Railway free tier has 512MB RAM
- Consider upgrading plan
- Optimize dependencies in requirements.txt
```

**Problem:** Model cache errors
```
Solution: Already handled in Dockerfile
- Creates /app/model_cache directory
- Sets HF_HOME environment variable
```

### Frontend Issues

**Problem:** Build fails with memory error
```
Solution: Increase Railway build memory
- Go to Service → Settings
- Add environment variable: NODE_OPTIONS=--max-old-space-size=4096
```

**Problem:** 404 on routes
```
Solution: Angular routing needs proper configuration
- Ensure railway.json has correct start command
- Using 'serve' with -s flag handles SPA routing
```

**Problem:** API calls failing
```
Solution: Check CORS and environment
- Verify environment.prod.ts has correct URLs
- Check browser console for CORS errors
- Ensure backend CORS includes frontend URL
```

---

## 💰 Railway Pricing (Free Tier)

**What you get FREE:**
- $5 worth of usage per month
- 512MB RAM per service
- 1GB disk space
- Shared CPU
- Public networking included

**Enough for development and demo!**

---

## 📊 Your Final Architecture

```
┌─────────────────────────────────────────────────────┐
│                                                     │
│  👤 Users                                           │
│      ↓                                              │
│  🎨 Frontend (Railway)                              │
│      ↓                                              │
│  ⚙️  Backend (Render)                               │
│      ↓                                              │
│  🤖 AI Service (Railway)                            │
│      ↓                                              │
│  💾 MongoDB Atlas                                   │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**Your URLs:**
- Frontend: `https://frontend-production-xxxx.up.railway.app`
- Backend: `https://smart-hire-backend.onrender.com`
- AI Service: `https://ai-service-production-xxxx.up.railway.app`

---

## 🎯 Estimated Timeline

- **AI Service Setup:** 5 minutes
- **AI Service Deployment:** 5-8 minutes
- **Frontend Setup:** 3 minutes
- **Frontend Deployment:** 4-6 minutes
- **Backend Updates:** 3 minutes
- **Testing:** 5-10 minutes

**Total Time:** 25-35 minutes ⚡

---

## 🆘 Need Help?

### Railway Support
- Docs: https://docs.railway.app
- Discord: https://discord.gg/railway
- Status: https://status.railway.app

### Common Commands

```bash
# View logs in Railway
# Go to Dashboard → Service → Deployments → View Logs

# Rollback deployment
# Dashboard → Service → Deployments → Click on previous deployment → Rollback

# Restart service
# Dashboard → Service → Settings → Restart
```

---

## 🚀 Ready to Deploy?

Follow the steps in order:

1. ✅ **Deploy AI Service first** (PART 1)
2. ✅ **Deploy Frontend** (PART 2)  
3. ✅ **Update Backend** (PART 3)
4. ✅ **Test Everything**

Let me know when you're ready to start, or if you need help with any step! 🎉
