import { useSyncExternalStore } from 'react'
import { authStore } from './authStore'

// Az aktuális JWT token (vagy null). A komponens újrarajzolódik, ha a felhasználó be- vagy kilép.
export function useAuthToken(): string | null {
  return useSyncExternalStore(authStore.subscribe, authStore.getToken)
}
