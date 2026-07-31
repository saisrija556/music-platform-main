"use client";

import { useRef, useState } from "react";
import { api } from "@/lib/api";
import type { AlbumSearchResult } from "@/lib/types";

let currentAudio: HTMLAudioElement | null = null;

interface AlbumCardProps {
  album: AlbumSearchResult;
  action?: React.ReactNode;
  footer?: React.ReactNode;
}

export default function AlbumCard({
  album,
  action,
  footer,
}: AlbumCardProps) {
  const audioRef = useRef<HTMLAudioElement>(null);

  const [playing, setPlaying] = useState(false);
  const [loaded, setLoaded] = useState(false);

  async function playSong() {
    if (!audioRef.current) return;

    if (!loaded) {
      try {
        const tracks = await api.getAlbumTracks(album.appleCatalogId);

        if (tracks.length === 0) {
          alert("No preview available");
          return;
        }

        audioRef.current.src = tracks[0].previewUrl;
        setLoaded(true);
      } catch {
        alert("Unable to load preview");
        return;
      }
    }

    // Pause any other audio that's currently playing
    if (currentAudio && currentAudio !== audioRef.current) {
      currentAudio.pause();
      currentAudio.currentTime = 0;
    }

    if (playing) {
      audioRef.current.pause();
      setPlaying(false);

      if (currentAudio === audioRef.current) {
        currentAudio = null;
      }

      return;
    }

    currentAudio = audioRef.current;

    try {
      await audioRef.current.play();
      setPlaying(true);
    } catch {
      setPlaying(false);
    }
  }

  return (
    <article className="group flex flex-col overflow-hidden rounded-2xl border border-white/10 bg-white/5 shadow-lg backdrop-blur-sm transition-all duration-300 hover:-translate-y-2 hover:scale-[1.02] hover:border-cyan-400/40 hover:shadow-2xl hover:shadow-cyan-500/10">
      <div className="relative aspect-square overflow-hidden bg-slate-800">
        {album.artworkUrl ? (
          <>
            <img
              src={album.artworkUrl}
              alt={album.title}
              className="h-full w-full rounded-t-2xl object-cover transition duration-500 group-hover:scale-110"
            />

            <div className="absolute inset-0 flex items-center justify-center bg-black/0 transition-all duration-300 group-hover:bg-black/40">
              <button
                onClick={playSong}
                className="flex h-16 w-16 scale-75 items-center justify-center rounded-full bg-cyan-500 text-2xl text-white opacity-0 shadow-xl transition-all duration-300 group-hover:scale-100 group-hover:opacity-100 hover:bg-cyan-400"
              >
                {playing ? "❚❚" : "▶"}
              </button>
            </div>
          </>
        ) : (
          <div className="flex h-full items-center justify-center text-slate-500">
            No artwork
          </div>
        )}
      </div>

      <div className="flex flex-1 flex-col gap-3 p-4">
        <h3 className="line-clamp-2 font-semibold text-white">
          {album.title}
        </h3>

        <p className="text-sm text-slate-400">
          {album.artistName}
        </p>

        <div className="flex flex-wrap gap-2 text-xs text-slate-300">
          {album.genre && (
            <span className="rounded-full bg-white/10 px-3 py-1">
              {album.genre}
            </span>
          )}

          {album.releaseDate && (
            <span className="rounded-full bg-white/10 px-3 py-1">
              {album.releaseDate.slice(0, 4)}
            </span>
          )}

          {album.trackCount != null && (
            <span className="rounded-full bg-white/10 px-3 py-1">
              {album.trackCount} tracks
            </span>
          )}
        </div>

        {action && <div className="mt-auto pt-2">{action}</div>}

        {footer}
      </div>

      <audio
        ref={audioRef}
        controls
        className="w-full"
        onPlay={() => setPlaying(true)}
        onPause={() => {
          setPlaying(false);
          if (currentAudio === audioRef.current) {
            currentAudio = null;
          }
        }}
        onEnded={() => {
          setPlaying(false);
          if (currentAudio === audioRef.current) {
            currentAudio = null;
          }
        }}
      />
    </article>
  );
}
