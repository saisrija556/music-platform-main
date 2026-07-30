"use client";

import { useEffect, useState } from "react";
import {
  GenreBarChart,
  GenrePieChart,
  ReleasesByYearChart,
  TopArtistsChart,
} from "@/components/AnalyticsCharts";
import { api } from "@/lib/api";
import type { AnalyticsSummary } from "@/lib/types";

export default function AnalyticsPage() {
  const [data, setData] = useState<AnalyticsSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .getAnalyticsSummary()
      .then(setData)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load analytics"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">Analytics Dashboard</h1>
        <p className="mt-1 text-slate-400">Insights computed from your saved album library.</p>
      </div>

      {error && <p className="text-sm text-red-400">{error}</p>}

      {!loading && data && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <StatCard label="Total Albums" value={String(data.totalAlbums)} />
          <StatCard
            label="Avg Rating"
            value={data.averageRating != null ? String(data.averageRating) : "—"}
          />
          <StatCard label="Genres" value={String(Object.keys(data.albumsByGenre).length)} />
          <StatCard label="Artists" value={String(data.topArtists.length)} />
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <GenreBarChart data={data} loading={loading} minItems={1} />
        <GenrePieChart data={data} loading={loading} minItems={1} />
        <ReleasesByYearChart data={data} loading={loading} minItems={1} />
        <TopArtistsChart data={data} loading={loading} minItems={1} />
      </div>
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-white/10 bg-white/5 p-4">
      <p className="text-xs uppercase tracking-wide text-slate-500">{label}</p>
      <p className="mt-1 text-2xl font-bold text-white">{value}</p>
    </div>
  );
}
