# 🚀 QUICK DEPLOYMENT CHECKLIST - Backend Only

## ✅ Pre-Deployment Checklist

- [ ] All code changes committed
- [ ] MongoDB connection string ready
- [ ] GitHub account ready
- [ ] Render account created (render.com)

---

## 📝 Step-by-Step (5 Minutes)

### 1️⃣ Push to GitHub (2 min)
```bash
cd "E:\New Data\AutoHireX"
git add .
git commit -m "Ready for Render deployment"
git push
```

If you haven't set up GitHub yet:
```bash
# Create repo on github.com first, then:
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git branch -M main
git push -u origin main
```

---

### 2️⃣ Create Render Service (3 min)

1. Go to: https://render.com
2. Sign up/Login with GitHub
3. Click "New +" → "Web Service"
4. Connect your GitHub repository
5. Select your repository

**Configure:**
- **Name**: `smart-hire-backend`
- **Region**: Oregon (USA)
- **Root Directory**: `backend` ⚠️ IMPORTANT!
- **Runtime**: Docker
- **Instance Type**: Free

---

### 3️⃣ Add Environment Variables (1 min)

Click "Environment" and add ONLY these 2:

**Variable 1:**
```
Key: SPRING_DATA_MONGODB_URI
Value: mongodb+srv://alok992955:AlokKushal9929@smarthiredb.4kbcmmh.mongodb.net/SmartHireDB?retryWrites=true&w=majority&appName=SmartHireDB&tls=true&tlsAllowInvalidCertificates=true
```

**Variable 2:**
```
Key: SPRING_DATA_MONGODB_DATABASE
Value: SmartHireDB
```

---

### 4️⃣ Deploy! (10 min build time)

- Click "Create Web Service"
- Wait for build to complete
- Watch logs for "Started SmartHireBackendApplication"

---

## 🎯 After Deployment

Your backend will be live at:
```
https://smart-hire-backend.onrender.com
```

### ✅ Test It:

1. **Health Check:**
   ```
   https://smart-hire-backend.onrender.com/actuator/health
   ```
   Should return: `{"status":"UP"}`

2. **Your API Endpoints:**
   ```
   https://smart-hire-backend.onrender.com/api/...
   ```

---

## 🔄 When You Deploy Frontend/AI Later

### Update these environment variables:

**After Frontend Deployment:**
```
FRONTEND_URL = https://your-frontend.onrender.com
```

**After AI Service Deployment:**
```
AI_SERVICE_URL = https://your-ai-service.onrender.com
FASTAPI_BASE_URL = https://your-ai-service.onrender.com
```

**How to update:**
1. Render Dashboard → Your Service → Environment
2. Add new variables
3. Service auto-redeploys (2-3 minutes)

---

## 🚨 Common Issues & Fixes

### Issue: Build fails
✅ Check Root Directory is set to `backend`
✅ Check Dockerfile exists in backend folder

### Issue: Health check fails
✅ Wait 2-3 minutes after deploy completes
✅ Check MongoDB connection string is correct

### Issue: MongoDB connection fails
✅ Go to MongoDB Atlas → Network Access
✅ Add IP: `0.0.0.0/0` (allow all for testing)
✅ Check username/password in connection string

### Issue: 404 on all endpoints
✅ Your app is running! Just use correct API paths
✅ Check your controller mappings

---

## 📞 Need Help?

- **Render Docs**: https://render.com/docs
- **Check Logs**: Render Dashboard → Logs tab
- **MongoDB Atlas**: https://cloud.mongodb.com

---

## 🎉 Success Indicators

✅ Build completes without errors
✅ Deploy status shows "Live" (green)
✅ Health endpoint returns UP
✅ Logs show "Started SmartHireBackendApplication"

**You're live! 🚀**

---

## 📌 Save This Info

**Your Backend URL:**
```
https://smart-hire-backend.onrender.com
```

**Health Check:**
```
https://smart-hire-backend.onrender.com/actuator/health
```

**Use this URL in your frontend and AI service configs!**
