# Music Catalog Insights Platform — Project Brief

See [README.md](./README.md) for setup and architecture. This file preserves the original project specification.

## Overview

Build a full-stack **Music Catalog Insights Platform** — search a public music catalog, save albums to a personal library, view analytics, and get AI-generated recommendations.

**Tech stack:** Java 17+ / Spring Boot 3.x · React / Next.js (App Router) · PostgreSQL · Spring Data JPA · JWT auth

**Entity focus:** Albums — richer metadata (genre, release date, track count, price) for meaningful analytics.

**AI feature:** `GET /api/insights/recommendations` — 3–5 recommendations with rationale (OpenAI or heuristic fallback).

## Build Order Completed

1. ✅ Backend scaffold (Spring Boot, JPA entities, PostgreSQL)
2. ✅ JWT auth (register/login/filter)
3. ✅ iTunes proxy + Caffeine caching
4. ✅ Library CRUD + validation + error handling
5. ✅ Frontend (auth, search w/ debounce, library, layout)
6. ✅ Analytics dashboard (4 Recharts)
7. ✅ Recommendations endpoint + fallback + insights page
8. ✅ Docker, `.env.example`, README

## Preferences Chosen

- **LLM:** OpenAI (`gpt-4o-mini`) when `OPENAI_API_KEY` is set
- **JWT storage:** localStorage (documented trade-off in README)
- **JWT expiry:** 24 hours (`JWT_EXPIRATION_MS=86400000`)
