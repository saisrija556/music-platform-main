# Music Catalog Insights - Render Deployment Guide

## Prerequisites
- A **GitHub account** with your project pushed to a repository
- A **Render account** (free tier available at https://render.com)
- Optional: OpenAI API key for AI-powered recommendations

## Step-by-Step Deployment

### 1. Push Your Project to GitHub
```bash
cd /Users/saisrija/Desktop/music-catalog-insights
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/music-catalog-insights.git
git branch -M main
git push -u origin main
```

### 2. Connect Render to GitHub
1. Go to https://dashboard.render.com
2. Click **"+ New"** > **"Blueprint"**
3. Connect your GitHub account and select the `music-catalog-insights` repository
4. Render will auto-detect the `render.yaml` file

### 3. Set Required Environment Variables
In the Render dashboard, before deploying, set these secrets:

**For Backend:**
- `JWT_SECRET` — Generate a strong 256-bit key:
  ```bash
  openssl rand -base64 32
  ```
  Example: `your-generated-random-secret-here`

- `OPENAI_API_KEY` (optional, for AI recommendations)
  - If you have an OpenAI API key, paste it here
  - If not, recommendations will use heuristic fallback (still works!)

**For Frontend:**
- `NEXT_PUBLIC_API_URL` — Will be auto-set to your backend URL

### 4. Custom Render.yaml Setup (Alternative)
If you prefer manual setup instead of Blueprint:

1. Go to Render dashboard > **"+ New"** > **"PostgreSQL"**
   - Name: `music-catalog-db`
   - Plan: Free

2. Create Backend Service:
   - **Name:** `music-catalog-backend`
   - **GitHub Repo:** Your repo
   - **Root Directory:** `backend/`
   - **Runtime:** Docker
   - **Plan:** Free
   - **Add Environment Variables:**
     - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://[postgres-host]:[port]/[db-name]`
     - `SPRING_DATASOURCE_USERNAME`: From PostgreSQL dashboard
     - `SPRING_DATASOURCE_PASSWORD`: From PostgreSQL dashboard
     - `JWT_SECRET`: Your generated secret
     - `CORS_ALLOWED_ORIGINS`: `https://music-catalog-frontend.onrender.com`

3. Create Frontend Service:
   - **Name:** `music-catalog-frontend`
   - **GitHub Repo:** Your repo
   - **Root Directory:** `frontend/`
   - **Runtime:** Node
   - **Build Command:** `npm install && npm run build`
   - **Start Command:** `npm run start`
   - **Plan:** Free
   - **Add Environment Variable:**
     - `NEXT_PUBLIC_API_URL`: Your backend service URL (e.g., `https://music-catalog-backend.onrender.com`)

### 5. Verify Deployment
Once services are deployed:
1. Visit your frontend URL (e.g., `https://music-catalog-frontend.onrender.com`)
2. Create a new account or login
3. Search for songs and try to play them
4. Check backend logs if issues occur: Dashboard > Service > Logs

## Troubleshooting

**"Failed to connect to API"**
- Verify `NEXT_PUBLIC_API_URL` is set correctly in frontend
- Check backend service is running (green "Live" status)
- Check logs for Java/Spring errors

**"Database connection failed"**
- Ensure PostgreSQL credentials match in `application.yml`
- Verify database is created

**"No preview available" even after fallback**
- This is normal for some albums without iTunes previews
- Recommendations still work based on genre/artist

**Frontend shows old version**
- Render caches builds. Force redeploy: Dashboard > Service > Manual Deploy

## Scaling Up (Paid)
- Upgrade PostgreSQL plan for production data
- Upgrade backend/frontend plans for better performance
- Add Redis cache layer for recommendations

## Useful Links
- Render Dashboard: https://dashboard.render.com
- Render Docs: https://render.com/docs
- Spring Boot on Render: https://render.com/docs/docker
- Next.js on Render: https://render.com/docs/deploy-next-js
