import { useSyncExternalStore } from 'react'
import { sessionStore, type Session, type SessionState } from './sessionStore'

// A munkamenet állapota; a komponens újrarajzolódik, ha a felhasználó be- vagy kilép
export function useSessionState(): SessionState {
  return useSyncExternalStore(sessionStore.subscribe, sessionStore.getState)
}

export function useSession(): Session | null {
  return useSessionState().session
}
