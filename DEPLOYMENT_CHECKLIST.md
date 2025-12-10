# ⚡ Quick Railway Deployment Checklist

## 🎯 Before You Start

- [ ] Railway account created (https://railway.app)
- [ ] GitHub repository ready
- [ ] MongoDB Atlas connection string
- [ ] Google Gemini API key

---

## 📝 Deployment Steps

### 1️⃣ AI Service (FastAPI)

- [ ] Push code to GitHub
- [ ] Create new Railway project
- [ ] Connect GitHub repository
- [ ] Select `ai-service` folder
- [ ] Add environment variables:
  - [ ] `GOOGLE_API_KEY`
  - [ ] `MONGODB_URI`
  - [ ] `PORT=5001`
- [ ] Deploy and wait (~5 mins)
- [ ] Test: `https://your-ai-url/health`
- [ ] **Save AI Service URL** ✏️: _______________________

### 2️⃣ Frontend (Angular)

- [ ] Update `frontend/src/environments/environment.prod.ts`:
  ```typescript
  apiUrl: "https://smart-hire-backend.onrender.com/api"
  aiServiceUrl: "https://your-ai-service.up.railway.app"
  ```
- [ ] Commit and push changes
- [ ] Create new Railway service
- [ ] Select `frontend` folder
- [ ] Build Command: `npm run build:railway`
- [ ] Start Command: `npx serve dist/smart-hire-frontend/browser -s -l 3000`
- [ ] Deploy and wait (~5 mins)
- [ ] **Save Frontend URL** ✏️: _______________________

### 3️⃣ Update Backend (Render)

- [ ] Go to Render Dashboard → Backend Service
- [ ] Add environment variables:
  - [ ] `FRONTEND_URL=https://your-frontend.up.railway.app`
  - [ ] `AI_SERVICE_URL=https://your-ai-service.up.railway.app`
  - [ ] `FASTAPI_BASE_URL=https://your-ai-service.up.railway.app`
- [ ] Update CORS in code if needed
- [ ] Redeploy backend

---

## ✅ Testing

- [ ] AI Service health check works
- [ ] Frontend loads in browser
- [ ] Login functionality works
- [ ] Can create job posting
- [ ] Resume parsing works
- [ ] AI analysis returns results

---

## 🎉 You're Live!

**Your Production URLs:**
- Frontend: _______________________
- Backend: https://smart-hire-backend.onrender.com
- AI Service: _______________________

---

## 📞 Quick Links

- Railway Dashboard: https://railway.app/dashboard
- Render Dashboard: https://dashboard.render.com
- MongoDB Atlas: https://cloud.mongodb.com

---

## 🐛 Common Issues

**Build fails?**
→ Check logs in Railway Dashboard → Deployments

**App not loading?**
→ Verify environment variables are set correctly

**CORS errors?**
→ Update backend CORS with frontend URL

**AI service timeout?**
→ Railway free tier may need warm-up, try again

---

**Estimated Total Time:** 30 minutes ⏱️

Good luck! 🚀
