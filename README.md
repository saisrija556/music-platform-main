# Music Catalog Insights Platform

A full-stack web app for searching the iTunes album catalog, saving albums to a personal library, viewing analytics, and getting AI-powered recommendations.

## Live URLs

| Service  | URL |
|----------|-----|
| Frontend | _Deploy to Vercel — see [Deployment](#deployment)_ |
| Backend  | _Deploy to Render/Railway — see [Deployment](#deployment)_ |

## Entity Choice: Albums

This platform focuses on **albums** (not individual tracks or artist records). Albums provide richer structured metadata for analytics — genre, release date, track count, and price — making dashboard charts and AI recommendations more meaningful than track- or artist-only models.

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security (JWT)
- **Frontend:** Next.js 14 (App Router), React, Tailwind CSS, Recharts
- **Database:** PostgreSQL
- **External APIs:** iTunes Search API (proxied), OpenAI (optional)

## Why PostgreSQL?

The data is relational with a fixed, well-understood shape. Analytics queries (group by genre, group by release year, average ratings) benefit from SQL aggregation and indexes on `user_id` and `apple_catalog_id`.

## Local Setup

### Prerequisites

- Docker & Docker Compose (recommended), **or** Java 17+, Maven, and PostgreSQL 16+
- Node.js 18+

### 1. Clone and configure

```bash
git clone <repo-url>
cd music-catalog-insights

cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env.local
```

Optional: set `OPENAI_API_KEY` in `backend/.env` for LLM recommendations.

### 2. Start backend + database (Docker)

```bash
docker compose up --build
```

Backend runs at `http://localhost:8080`.

### 2b. Start backend manually (without Docker)

```bash
# Start PostgreSQL locally, create database `music_catalog`
cd backend
mvn spring-boot:run
```

### 3. Start frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:3000`.

## API Overview

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | No | Register with email + password |
| POST | `/api/auth/login` | No | Login, returns JWT |
| GET | `/api/search?query=&type=album&limit=25` | Yes | Search iTunes (proxied + cached) |
| GET | `/api/library?page=&size=&sort=` | Yes | Paginated library |
| POST | `/api/library` | Yes | Save album |
| PUT | `/api/library/{id}` | Yes | Update rating/notes |
| DELETE | `/api/library/{id}` | Yes | Remove album |
| GET | `/api/analytics/summary` | Yes | Aggregated analytics |
| GET | `/api/insights/recommendations` | Yes | 3–5 AI/heuristic recommendations |

## AI Recommendations

When `OPENAI_API_KEY` is set, the backend sends a summary of the user's library (genres, artists, eras) to **OpenAI (`gpt-4o-mini`)** and returns 3–5 album suggestions with one-line rationales, enriched via iTunes lookup.

**Fallback (no API key):** A genre-based heuristic picks the user's most common genre, searches iTunes for curated classics in that genre, and excludes albums already in the library. Results are cached per user for 15 minutes (Caffeine).

## JWT Storage Trade-off

The frontend stores the JWT in **localStorage** for simpler local development. This is vulnerable to XSS; **httpOnly cookies** would be more secure in production (requires backend cookie configuration and CSRF considerations). Documented here per project requirements.

## Deployment

### Backend (Render / Railway)

1. Create a PostgreSQL add-on
2. Deploy the `backend/` directory (Dockerfile included)
3. Set environment variables from `backend/.env.example`
4. Set `CORS_ALLOWED_ORIGINS` to your Vercel frontend URL

### Frontend (Vercel)

1. Import the `frontend/` directory
2. Set `NEXT_PUBLIC_API_URL` to your deployed backend URL
3. Deploy

## Schema Note

`library_items` uses a composite unique constraint on `(user_id, apple_catalog_id)` so multiple users can save the same album — adjusted from a global unique on `apple_catalog_id` for multi-user correctness.

## Known Trade-offs / Future Work

- JWT in localStorage instead of httpOnly cookies
- No refresh tokens (24h expiry)
- In-memory Caffeine cache (Redis for multi-instance deployments)
- Recommendation cache invalidates on TTL only, not on library changes
- Unit test coverage is minimal
- iTunes API rate limits not explicitly handled beyond caching

## Project Structure

```
music-catalog-insights/
├── backend/          # Spring Boot API
├── frontend/         # Next.js app
├── docker-compose.yml
└── README.md
```
