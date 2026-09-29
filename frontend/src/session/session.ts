import { toast } from 'sonner'
import { queryKeys } from '../api/queryKeys'
import type { AuthResponse } from '../api/types'
import { queryClient } from '../lib/queryClient'
import { sessionStore, type SessionEndReason } from './sessionStore'

// Belépés vagy regisztráció után: az előző felhasználó gyorsítótárazott adatai törlődnek, a válaszban kapott
// felhasználói adatok pedig rögtön a gyorsítótárba kerülnek (a fejléc így azonnal mutatja a nevet)
export function startSession(auth: AuthResponse) {
  queryClient.clear()
  queryClient.setQueryData(queryKeys.me, auth.user)
  sessionStore.start({ token: auth.token, expiresAt: auth.expiresAt ?? null })
}

// Kilépés, lejárt vagy érvénytelen token: a token és a gyorsítótár is törlődik.
// Többszöri hívás (pl. egyszerre több 401-es válasz) nem okoz gondot.
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

// A felhasználó saját kilépése (a menü "Kijelentkezés" gombja)
export function logout() {
  endSession('logout')
  toast.success('Sikeresen kijelentkeztél.')
}
