"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import AlbumCard from "@/components/AlbumCard";
import { api } from "@/lib/api";
import type { LibraryItem } from "@/lib/types";

export default function LibraryPage() {
  const [items, setItems] = useState<LibraryItem[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [rating, setRating] = useState<number>(3);
  const [notes, setNotes] = useState("");
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  const loadLibrary = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await api.getLibrary(page);
      setItems(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load library");
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    loadLibrary();
  }, [loadLibrary]);

  function startEdit(item: LibraryItem) {
    setEditingId(item.id);
    setRating(item.userRating || 3);
    setNotes(item.userNotes || "");
  }

  async function saveEdit(id: number) {
    try {
      await api.updateLibraryItem(id, { userRating: rating, userNotes: notes });
      setEditingId(null);
      loadLibrary();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Update failed");
    }
  }

  async function deleteItem(id: number) {
    try {
      await api.deleteLibraryItem(id);
      setDeleteConfirmId(null);
      loadLibrary();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-violet-500 border-t-transparent" />
      </div>
    );
  }

  if (!loading && items.length === 0 && page === 0) {
    return (
      <div className="space-y-4 text-center">
        <h1 className="text-3xl font-bold text-white">Your Library</h1>
        <div className="rounded-xl border border-dashed border-white/10 py-20">
          <p className="text-lg text-slate-300">Your library is empty — go search for albums.</p>
          <Link
            href="/search"
            className="mt-4 inline-block rounded-lg bg-violet-600 px-6 py-2 text-sm font-medium text-white hover:bg-violet-500"
          >
            Search Albums
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white">Your Library</h1>
        <p className="mt-1 text-slate-400">Manage ratings and notes for saved albums.</p>
      </div>

      {error && <p className="text-sm text-red-400">{error}</p>}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {items.map((item) => (
          <AlbumCard
            key={item.id}
            album={{
              appleCatalogId: item.appleCatalogId,
              title: item.title,
              artistName: item.artistName,
              genre: item.genre,
              releaseDate: item.releaseDate,
              trackCount: item.trackCount,
              artworkUrl: item.artworkUrl,
            }}
            footer={
              <div className="mt-3 space-y-2 border-t border-white/10 pt-3">
                {editingId === item.id ? (
                  <>
                    <label className="block text-xs text-slate-400">
                      Rating (1–5)
                      <select
                        value={rating}
                        onChange={(e) => setRating(Number(e.target.value))}
                        className="mt-1 w-full rounded border border-white/10 bg-slate-900 px-2 py-1 text-sm"
                      >
                        {[1, 2, 3, 4, 5].map((n) => (
                          <option key={n} value={n}>
                            {n} ★
                          </option>
                        ))}
                      </select>
                    </label>
                    <label className="block text-xs text-slate-400">
                      Notes
                      <textarea
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        rows={2}
                        className="mt-1 w-full rounded border border-white/10 bg-slate-900 px-2 py-1 text-sm"
                      />
                    </label>
                    <div className="flex gap-2">
                      <button
                        onClick={() => saveEdit(item.id)}
                        className="flex-1 rounded bg-violet-600 py-1.5 text-xs text-white"
                      >
                        Save
                      </button>
                      <button
                        onClick={() => setEditingId(null)}
                        className="flex-1 rounded border border-white/10 py-1.5 text-xs"
                      >
                        Cancel
                      </button>
                    </div>
                  </>
                ) : (
                  <>
                    {item.userRating && (
                      <p className="text-xs text-amber-400">Rating: {"★".repeat(item.userRating)}</p>
                    )}
                    {item.userNotes && (
                      <p className="text-xs text-slate-400 line-clamp-2">{item.userNotes}</p>
                    )}
                    <div className="flex gap-2">
                      <button
                        onClick={() => startEdit(item)}
                        className="flex-1 rounded border border-white/10 py-1.5 text-xs hover:bg-white/5"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => setDeleteConfirmId(item.id)}
                        className="flex-1 rounded border border-red-500/30 py-1.5 text-xs text-red-400 hover:bg-red-500/10"
                      >
                        Delete
                      </button>
                    </div>
                  </>
                )}
                {deleteConfirmId === item.id && (
                  <div className="rounded border border-red-500/30 bg-red-500/10 p-2 text-xs">
                    <p className="text-red-200">Remove this album?</p>
                    <div className="mt-2 flex gap-2">
                      <button
                        onClick={() => deleteItem(item.id)}
                        className="rounded bg-red-600 px-2 py-1 text-white"
                      >
                        Confirm
                      </button>
                      <button
                        onClick={() => setDeleteConfirmId(null)}
                        className="rounded border border-white/10 px-2 py-1"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                )}
              </div>
            }
          />
        ))}
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-4">
          <button
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
            className="rounded border border-white/10 px-4 py-2 text-sm disabled:opacity-40"
          >
            Previous
          </button>
          <span className="text-sm text-slate-400">
            Page {page + 1} of {totalPages}
          </span>
          <button
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
            className="rounded border border-white/10 px-4 py-2 text-sm disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
