"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { clearAuth, getEmail } from "@/lib/auth";

const navItems = [
  { href: "/search", label: "Search" },
  { href: "/library", label: "Library" },
  { href: "/analytics", label: "Analytics" },
  { href: "/insights", label: "Insights" },
];

export default function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const email = getEmail();

  const isAuthPage = pathname === "/login" || pathname === "/register";

  function logout() {
    clearAuth();
    router.push("/login");
  }

  if (isAuthPage) {
    return <>{children}</>;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-cyan-950 text-slate-100">
      <header className="sticky top-0 z-50 border-b border-white/10 bg-slate-950/80 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6">
          <Link href="/search" className="text-lg font-bold tracking-tight text-white">
            Music Catalog Insights
          </Link>
          <nav className="hidden gap-1 sm:flex">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`rounded-lg px-3 py-2 text-sm font-medium transition ${
                  pathname === item.href
                    ? "bg-pink-400 text-white hover:bg-pink-300 transition-colors" : "bg-white/5 text-slate-300 hover:bg-white/10"
                }`}
              >
                {item.label}
              </Link>
            ))}
          </nav>
          <div className="flex items-center gap-3">
            {email && (
              <span className="hidden text-xs text-slate-400 sm:inline">{email}</span>
            )}
            <button
              onClick={logout}
              className="rounded-lg border border-white/10 px-3 py-1.5 text-sm text-slate-300 hover:bg-white/10"
            >
              Log out
            </button>
          </div>
        </div>
        <nav className="flex gap-1 overflow-x-auto px-4 pb-3 sm:hidden">
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={`whitespace-nowrap rounded-lg px-3 py-1.5 text-xs font-medium ${
                pathname === item.href ? "bg-pink-400 text-white hover:bg-pink-300 transition-colors" : "bg-white/5 text-slate-300 hover:bg-white/10"
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  );
}
