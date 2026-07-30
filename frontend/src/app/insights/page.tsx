"use client";

import { useEffect, useState } from "react";
import AlbumCard from "@/components/AlbumCard";
import { api } from "@/lib/api";
import type { Recommendation } from "@/lib/types";

export default function InsightsPage() {
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [source, setSource] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .getRecommendations()
      .then((res) => {
        setRecommendations(res.recommendations);
        setSource(res.source);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load recommendations"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">AI Recommendations</h1>
        <p className="mt-1 text-slate-400">
          Personalized album picks based on your library&apos;s genres, artists, and eras.
        </p>
      </div>

      {loading && (
        <div className="flex flex-col items-center justify-center gap-4 py-20">
          <div className="h-10 w-10 animate-spin rounded-full border-2 border-violet-500 border-t-transparent" />
          <p className="text-slate-400">Generating recommendations… this may take a few seconds.</p>
        </div>
      )}

      {error && <p className="text-sm text-red-400">{error}</p>}

      {!loading && !error && source && (
        <p className="text-xs uppercase tracking-wide text-slate-500">
          Powered by: {source === "openai" ? "OpenAI" : source === "heuristic" ? "Genre heuristic (no API key)" : source}
        </p>
      )}

      {!loading && recommendations.length === 0 && !error && (
        <div className="rounded-xl border border-dashed border-white/10 py-16 text-center">
          <p className="text-lg text-slate-300">Add albums to your library to get recommendations.</p>
        </div>
      )}

      {!loading && recommendations.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {recommendations.map((rec, i) => (
            <div key={`${rec.title}-${i}`} className="space-y-2">
              <AlbumCard
                album={{
                  appleCatalogId: rec.appleCatalogId || i,
                  title: rec.title,
                  artistName: rec.artistName,
                  genre: rec.genre,
                  artworkUrl: rec.artworkUrl,
                }}
              />
              <p className="rounded-lg bg-violet-500/10 px-3 py-2 text-sm text-violet-200">
                {rec.rationale}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
