import { queryClient } from '../lib/queryClient'
import { authStore } from './authStore'

// Belépés: az előző felhasználó gyorsítótárazott adatai törlődnek, majd eltároljuk az új tokent
export function startSession(token: string) {
  queryClient.clear()
  authStore.setToken(token)
}

// Kilépés vagy lejárt token: a token és a gyorsítótár is törlődik
export function endSession() {
  authStore.clear()
  queryClient.clear()
}
