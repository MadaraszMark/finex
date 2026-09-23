import { LogOut } from 'lucide-react'
import type { UserResponse } from '../api/types'
import { endSession } from '../auth/session'
import Logo from './Logo'

export default function AppHeader({ user }: { user?: UserResponse }) {
  return (
    <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/80 backdrop-blur-sm">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <Logo />
        <div className="flex items-center gap-4">
          {user && (
            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium text-slate-800">
                {user.lastName} {user.firstName}
              </p>
              <p className="text-xs text-slate-500">{user.email}</p>
            </div>
          )}
          <button
            type="button"
            onClick={endSession}
            className="inline-flex items-center gap-2 rounded-xl px-3 py-2 text-sm font-medium text-slate-600 ring-1 ring-slate-200 transition hover:bg-slate-50 hover:text-slate-900"
          >
            <LogOut className="size-4" />
            Kijelentkezés
          </button>
        </div>
      </div>
    </header>
  )
}
