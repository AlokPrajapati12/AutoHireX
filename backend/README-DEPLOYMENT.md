# Smart Hire Backend - Render Deployment

## 🚀 Deployed on Render

This Spring Boot backend is configured for deployment on Render.com

### 📦 Files for Deployment

- `Dockerfile` - Multi-stage Docker build configuration
- `render.yaml` - Render service configuration
- `.dockerignore` - Files to exclude from Docker build
- `DEPLOYMENT-CHECKLIST.md` - Quick deployment guide

### 🔗 Live URL (After Deployment)

```
https://smart-hire-backend.onrender.com
```

### 🏥 Health Check

```
https://smart-hire-backend.onrender.com/actuator/health
```

### 🔧 Environment Variables Required

Only these 2 are required for initial deployment:

1. `SPRING_DATA_MONGODB_URI` - MongoDB Atlas connection string
2. `SPRING_DATA_MONGODB_DATABASE` - Database name (SmartHireDB)

### ⚙️ Optional (Add Later)

- `FRONTEND_URL` - Frontend application URL
- `AI_SERVICE_URL` - FastAPI AI service URL
- `FASTAPI_BASE_URL` - Same as AI_SERVICE_URL

### 📝 Deployment Steps

See `DEPLOYMENT-CHECKLIST.md` for detailed steps.

**Quick Start:**
1. Push to GitHub
2. Create Web Service on Render
3. Set Root Directory to `backend`
4. Select Docker runtime
5. Add environment variables
6. Deploy!

### 🛠️ Tech Stack

- Java 17
- Spring Boot 3.5.7
- MongoDB
- Spring Security + JWT
- Docker

### 📊 Free Tier Notes

- Application sleeps after 15 minutes of inactivity
- First request after sleep takes 30-60 seconds
- Sufficient for development and testing

### 🔄 Updating Deployment

Any push to your main branch will trigger auto-deployment on Render.

```bash
git add .
git commit -m "Update backend"
git push
```

Render will automatically:
1. Pull latest code
2. Build Docker image
3. Deploy new version
4. Run health checks

### 🐛 Troubleshooting

**Build Fails:**
- Check Root Directory is set to `backend`
- Verify Dockerfile exists

**MongoDB Connection Fails:**
- Check Network Access in MongoDB Atlas
- Verify connection string format
- Add `0.0.0.0/0` to IP whitelist

**Application Won't Start:**
- Check logs in Render dashboard
- Verify environment variables
- Ensure port 8080 is used

### 📚 Resources

- [Render Documentation](https://render.com/docs)
- [Spring Boot on Render](https://render.com/docs/deploy-spring-boot)
- [MongoDB Atlas](https://cloud.mongodb.com)

---

**Last Updated:** December 2024
