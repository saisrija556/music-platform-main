"use client";

import { useCallback, useEffect, useState } from "react";
import AlbumCard from "@/components/AlbumCard";
import { api } from "@/lib/api";
import type { AlbumSearchResult } from "@/lib/types";

function SearchSkeleton() {
  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
      {Array.from({ length: 8 }).map((_, i) => (
        <div key={i} className="animate-pulse rounded-xl bg-white/5">
          <div className="aspect-square bg-slate-800" />
          <div className="space-y-2 p-4">
            <div className="h-4 rounded bg-slate-700" />
            <div className="h-3 w-2/3 rounded bg-slate-700" />
          </div>
        </div>
      ))}
    </div>
  );
}

export default function SearchPage() {
  const [query, setQuery] = useState("");
  const [debouncedQuery, setDebouncedQuery] = useState("");
  const [results, setResults] = useState<AlbumSearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [savingId, setSavingId] = useState<number | null>(null);
  const [savedIds, setSavedIds] = useState<Set<number>>(new Set());
  const [message, setMessage] = useState("");

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query.trim()), 450);
    return () => clearTimeout(timer);
  }, [query]);

  const runSearch = useCallback(async (q: string) => {
    if (!q) {
      setResults([]);
      return;
    }
    setLoading(true);
    setError("");
    try {
      const data = await api.search(q);
      setResults(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Search failed");
      setResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    runSearch(debouncedQuery);
  }, [debouncedQuery, runSearch]);

  async function saveAlbum(album: AlbumSearchResult) {
    setSavingId(album.appleCatalogId);
    setMessage("");
    try {
      await api.addToLibrary(album);
      setSavedIds((prev) => new Set(prev).add(album.appleCatalogId));
      setMessage(`Saved "${album.title}" to your library.`);
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "Failed to save");
    } finally {
      setSavingId(null);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">Search Albums</h1>
        <p className="mt-1 text-slate-400">Find albums via iTunes and save them to your library.</p>
      </div>

      <div className="relative">
        <input
          type="search"
          placeholder="Search by album, artist, or genre..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 pl-11 text-white placeholder:text-slate-500 outline-none focus:border-violet-500"
        />
        <span className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-500">⌕</span>
      </div>

      {message && (
        <p className="rounded-lg border border-violet-500/30 bg-violet-500/10 px-4 py-2 text-sm text-violet-200">
          {message}
        </p>
      )}
      {error && <p className="text-sm text-red-400">{error}</p>}

      {loading && <SearchSkeleton />}

      {!loading && debouncedQuery && results.length === 0 && !error && (
        <div className="rounded-xl border border-dashed border-white/10 py-16 text-center">
          <p className="text-lg text-slate-300">No results for &ldquo;{debouncedQuery}&rdquo;</p>
          <p className="mt-2 text-sm text-slate-500">Try a different search term.</p>
        </div>
      )}

      {!loading && !debouncedQuery && (
        <div className="rounded-xl border border-dashed border-white/10 py-16 text-center text-slate-500">
          Start typing to search the iTunes catalog.
        </div>
      )}

      {!loading && results.length > 0 && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {results.map((album) => {
            const saved = savedIds.has(album.appleCatalogId);
            return (
              <AlbumCard
                key={album.appleCatalogId}
                album={album}
                action={
                  <button
                    onClick={() => saveAlbum(album)}
                    disabled={saved || savingId === album.appleCatalogId}
                    className="w-full rounded-lg bg-violet-600 py-2 text-sm font-medium text-white hover:bg-violet-500 disabled:cursor-not-allowed disabled:bg-slate-700"
                  >
                    {saved
                      ? "Saved"
                      : savingId === album.appleCatalogId
                        ? "Saving..."
                        : "Save to Library"}
                  </button>
                }
              />
            );
          })}
        </div>
      )}
    </div>
  );
}
