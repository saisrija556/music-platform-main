"use client";

import type { AlbumSearchResult } from "@/lib/types";

interface AlbumCardProps {
  album: AlbumSearchResult;
  action?: React.ReactNode;
  footer?: React.ReactNode;
}

export default function AlbumCard({ album, action, footer }: AlbumCardProps) {
  return (
    <article className="flex flex-col overflow-hidden rounded-xl border border-white/10 bg-white/5 shadow-lg backdrop-blur-sm transition hover:border-violet-500/40">
      <div className="aspect-square bg-slate-800">
        {album.artworkUrl ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            src={album.artworkUrl}
            alt={album.title}
            className="h-full w-full object-cover"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-slate-500">No artwork</div>
        )}
      </div>
      <div className="flex flex-1 flex-col gap-2 p-4">
        <h3 className="line-clamp-2 font-semibold text-white">{album.title}</h3>
        <p className="text-sm text-slate-400">{album.artistName}</p>
        <div className="flex flex-wrap gap-2 text-xs text-slate-500">
          {album.genre && <span className="rounded bg-white/5 px-2 py-0.5">{album.genre}</span>}
          {album.releaseDate && (
            <span className="rounded bg-white/5 px-2 py-0.5">
              {album.releaseDate.slice(0, 4)}
            </span>
          )}
          {album.trackCount != null && (
            <span className="rounded bg-white/5 px-2 py-0.5">{album.trackCount} tracks</span>
          )}
        </div>
        {action && <div className="mt-auto pt-2">{action}</div>}
        {footer}
      </div>
    </article>
  );
}
