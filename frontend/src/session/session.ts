import { toast } from 'sonner'
import { queryKeys } from '../api/queryKeys'
import type { AuthResponse } from '../api/types'
import { queryClient } from '../lib/queryClient'
import { sessionStore, type SessionEndReason } from './sessionStore'

// Másik fülön be- vagy kilépés után azonnal törlődik a gyorsítótár (más adata ne látsszon)
sessionStore.onExternalChange(() => queryClient.clear())

// Belépéskor törlődik a gyorsítótár, a kapott felhasználó rögtön bekerül (a név azonnal látszik)
export function startSession(auth: AuthResponse) {
  queryClient.clear()
  queryClient.setQueryData(queryKeys.me, auth.user)
  sessionStore.start({ token: auth.token, expiresAt: auth.expiresAt ?? null })
}

// Ugyanaz a felhasználó, új token (pl. jelszócsere után): a gyorsítótár megmarad
export function replaceToken(auth: AuthResponse) {
  queryClient.setQueryData(queryKeys.me, auth.user)
  sessionStore.start({ token: auth.token, expiresAt: auth.expiresAt ?? null })
}

// Kilépés vagy lejárt token: token és gyorsítótár törlése (a többszöri hívás sem gond)
export function endSession(reason: SessionEndReason = 'logout') {
  if (!sessionStore.getState().session) {
    return
  }

  sessionStore.end(reason)
  queryClient.clear()

  if (reason === 'expired') {
    toast.info('Lejárt a munkameneted, kérjük, lépj be újra.', { id: 'session-expired' })
  }
}

// Saját kilépés: a token a szerveren is visszavonódik, de a kilépés nem várja meg a választ
export function logout() {
  const token = sessionStore.getToken()
  if (token) {
    void fetch('/api/users/me/logout', { method: 'POST', headers: { Authorization: `Bearer ${token}` }, keepalive: true }).catch(() => undefined)
  }
  endSession('logout')
  toast.success('Sikeresen kijelentkeztél.')
}
