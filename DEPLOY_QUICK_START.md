# Quick Deploy to Render - Steps

## Step 1: Prepare Your GitHub Repository

Make sure your project is on GitHub with all three key files:
- ✅ `render.yaml` (created)
- ✅ `backend/Dockerfile` (updated for Render)
- ✅ `frontend/package.json` with `"start"` script
- ✅ `backend/src/main/resources/application.yml` with env var support

If not on GitHub yet:
```bash
cd /Users/saisrija/Desktop/music-catalog-insights
git init
git add .
git commit -m "Initial commit - ready for Render deployment"
git remote add origin https://github.com/YOUR_USERNAME/music-catalog-insights.git
git branch -M main
git push -u origin main
```

## Step 2: Go to Render and Deploy

1. Visit: https://dashboard.render.com
2. Click **"+ New"** → **"Blueprint"**
3. Connect your GitHub account
4. Select `music-catalog-insights` repository
5. Click **"Update"** (Render will detect render.yaml)

## Step 3: Set Environment Variables

Before confirming deployment, Render will ask for environment variables:

**Required:**
- `JWT_SECRET` — Generate with:
  ```bash
  openssl rand -base64 32
  ```
  Paste the output here

**Optional:**
- `OPENAI_API_KEY` — Your OpenAI key (leave blank if you don't have one)

## Step 4: Deploy

Click **"Deploy"** and wait 5-10 minutes for:
- PostgreSQL database to initialize
- Backend to build and launch
- Frontend to build and launch

## Step 5: Verify

1. Check your frontend URL (e.g., `https://music-catalog-frontend.onrender.com`)
2. Refresh the page a few times (first load is slow on free tier)
3. Try searching and playing a song

## Troubleshooting

**Backend won't connect to database:**
- Wait 2-3 minutes after deployment
- Check backend logs: Dashboard → Service → Logs
- Verify `SPRING_DATASOURCE_URL` is correct

**Frontend shows "Cannot connect to API":**
- Verify `NEXT_PUBLIC_API_URL` env var is set
- Check backend service status (should be green)
- Clear browser cache and refresh

**No preview available but it worked locally:**
- This is expected for some albums on iTunes
- Check browser console for CORS errors
- iTunes may not have all proxies available

**Free tier services going to sleep:**
- Render puts free services to sleep after 15 min inactivity
- First request after sleep takes 30 sec
- Upgrade to paid plan for always-on

## After Deployment

### Production Checklist:
- [ ] Set a strong `JWT_SECRET`
- [ ] Set `OPENAI_API_KEY` if you want AI recommendations
- [ ] Upgrade database plan for production data
- [ ] Set up custom domain (in Render dashboard)
- [ ] Monitor logs regularly

### Database Backups:
- Render free tier doesn't backup automatically
- Upgrade to paid PostgreSQL for automatic backups
- Or export data periodically

### Scaling:
- Backend/Frontend: Free plans share CPU, upgrade for dedicated resources
- Database: Free plan has 256MB limit, upgrade for production data volume

---

**Frontend URL:** Will be shown after deployment
**Backend URL:** Will be shown after deployment
**Need help?** See `DEPLOYMENT_RENDER.md` for detailed guide
