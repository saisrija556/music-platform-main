"use client";

import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { AnalyticsSummary } from "@/lib/types";

const COLORS = ["#8b5cf6", "#a78bfa", "#c4b5fd", "#7c3aed", "#6d28d9", "#5b21b6", "#ddd6fe"];

interface ChartProps {
  data: AnalyticsSummary | null;
  loading: boolean;
  minItems?: number;
}

function ChartShell({
  title,
  loading,
  empty,
  children,
}: {
  title: string;
  loading: boolean;
  empty: boolean;
  children: React.ReactNode;
}) {
  return (
    <div className="rounded-xl border border-white/10 bg-white/5 p-4">
      <h3 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-400">{title}</h3>
      {loading ? (
        <div className="flex h-64 items-center justify-center">
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-violet-500 border-t-transparent" />
        </div>
      ) : empty ? (
        <p className="flex h-64 items-center justify-center text-sm text-slate-500">
          Add more albums to your library to see this chart.
        </p>
      ) : (
        children
      )}
    </div>
  );
}

export function GenreBarChart({ data, loading, minItems = 1 }: ChartProps) {
  const chartData = data
    ? Object.entries(data.albumsByGenre).map(([genre, count]) => ({ genre, count }))
    : [];
  const empty = !data || data.totalAlbums < minItems || chartData.length === 0;

  return (
    <ChartShell title="Albums by Genre" loading={loading} empty={empty}>
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis dataKey="genre" tick={{ fill: "#94a3b8", fontSize: 11 }} />
          <YAxis tick={{ fill: "#94a3b8", fontSize: 11 }} allowDecimals={false} />
          <Tooltip contentStyle={{ background: "#1e293b", border: "1px solid #334155" }} />
          <Bar dataKey="count" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </ChartShell>
  );
}

export function GenrePieChart({ data, loading, minItems = 1 }: ChartProps) {
  const chartData = data
    ? Object.entries(data.genreDistributionPercent).map(([name, value]) => ({ name, value }))
    : [];
  const empty = !data || data.totalAlbums < minItems || chartData.length === 0;

  return (
    <ChartShell title="Genre Distribution (%)" loading={loading} empty={empty}>
      <ResponsiveContainer width="100%" height={280}>
        <PieChart>
          <Pie data={chartData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label>
            {chartData.map((_, i) => (
              <Cell key={i} fill={COLORS[i % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip contentStyle={{ background: "#1e293b", border: "1px solid #334155" }} />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </ChartShell>
  );
}

export function ReleasesByYearChart({ data, loading, minItems = 1 }: ChartProps) {
  const chartData = data
    ? Object.entries(data.releasesByYear)
        .map(([year, count]) => ({ year, count }))
        .sort((a, b) => Number(a.year) - Number(b.year))
    : [];
  const empty = !data || data.totalAlbums < minItems || chartData.length === 0;

  return (
    <ChartShell title="Releases by Year" loading={loading} empty={empty}>
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis dataKey="year" tick={{ fill: "#94a3b8", fontSize: 11 }} />
          <YAxis tick={{ fill: "#94a3b8", fontSize: 11 }} allowDecimals={false} />
          <Tooltip contentStyle={{ background: "#1e293b", border: "1px solid #334155" }} />
          <Bar dataKey="count" fill="#a78bfa" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </ChartShell>
  );
}

export function TopArtistsChart({ data, loading, minItems = 1 }: ChartProps) {
  const chartData = data?.topArtists?.slice(0, 8) || [];
  const empty = !data || data.totalAlbums < minItems || chartData.length === 0;

  return (
    <ChartShell title="Top Artists by Saved Albums" loading={loading} empty={empty}>
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={chartData} layout="vertical" margin={{ left: 20 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis type="number" tick={{ fill: "#94a3b8", fontSize: 11 }} allowDecimals={false} />
          <YAxis
            type="category"
            dataKey="artistName"
            width={120}
            tick={{ fill: "#94a3b8", fontSize: 10 }}
          />
          <Tooltip contentStyle={{ background: "#1e293b", border: "1px solid #334155" }} />
          <Bar dataKey="albumCount" fill="#7c3aed" radius={[0, 4, 4, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </ChartShell>
  );
}
