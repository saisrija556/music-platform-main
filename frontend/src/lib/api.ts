import { getToken, clearAuth } from "./auth";
import type {
  AlbumSearchResult,
  AnalyticsSummary,
  AuthResponse,
  LibraryItem,
  PageResponse,
  RecommendationsResponse,
  TrackResponse,
} from "./types";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };
  if (token) {
    (headers as Record<string, string>)["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (res.status === 401) {
    clearAuth();
    if (typeof window !== "undefined") {
      window.location.href = "/login";
    }
    throw new Error("Unauthorized");
  }

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.message || `Request failed (${res.status})`);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return res.json();
}

export const api = {
  register: (email: string, password: string) =>
    request<AuthResponse>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),

  login: (email: string, password: string) =>
    request<AuthResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),

  search: (query: string, limit = 25) =>
    request<AlbumSearchResult[]>(
      `/api/search?query=${encodeURIComponent(query)}&type=album&limit=${limit}`
    ),

  getLibrary: (page = 0, size = 12, sort = "createdAt,desc") =>
    request<PageResponse<LibraryItem>>(
      `/api/library?page=${page}&size=${size}&sort=${sort}`
    ),

  addToLibrary: (album: Partial<AlbumSearchResult> & { appleCatalogId: number }) =>
    request<LibraryItem>("/api/library", {
      method: "POST",
      body: JSON.stringify(album),
    }),

  updateLibraryItem: (id: number, data: { userRating?: number; userNotes?: string }) =>
    request<LibraryItem>(`/api/library/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  deleteLibraryItem: (id: number) =>
    request<void>(`/api/library/${id}`, { method: "DELETE" }),

  getAnalyticsSummary: () => request<AnalyticsSummary>("/api/analytics/summary"),

  getRecommendations: () =>
    request<RecommendationsResponse>("/api/insights/recommendations"),
  getAlbumTracks: (albumId: number) =>
    request<TrackResponse[]>(`/api/search/${albumId}/tracks`),
};
