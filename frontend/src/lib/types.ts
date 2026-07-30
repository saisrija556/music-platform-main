export interface AuthResponse {
  token: string;
  email: string;
  userId: number;
}

export interface AlbumSearchResult {
  appleCatalogId: number;
  title: string;
  artistName: string;
  genre?: string;
  releaseDate?: string;
  trackCount?: number;
  artworkUrl?: string;
  price?: number;
}

export interface LibraryItem {
  id: number;
  appleCatalogId: number;
  title: string;
  artistName: string;
  genre?: string;
  releaseDate?: string;
  trackCount?: number;
  artworkUrl?: string;
  userRating?: number;
  userNotes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface AnalyticsSummary {
  totalAlbums: number;
  averageRating?: number;
  albumsByGenre: Record<string, number>;
  genreDistributionPercent: Record<string, number>;
  releasesByYear: Record<string, number>;
  topArtists: { artistName: string; albumCount: number }[];
  averageRatingByGenre: Record<string, number>;
  trackCountDistribution: Record<string, number>;
}

export interface Recommendation {
  title: string;
  artistName: string;
  genre?: string;
  rationale: string;
  appleCatalogId?: number;
  artworkUrl?: string;
}

export interface RecommendationsResponse {
  recommendations: Recommendation[];
  source: string;
}

export interface ApiError {
  message: string;
  status: number;
}
