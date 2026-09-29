import { storage } from '../lib/storage'

// A bejelentkezés adatai a backend AuthResponse-ából: a JWT token és a lejárat időpontja
export interface Session {
  token: string
  expiresAt: string | null
}

// Hogyan ért véget az előző munkamenet (ettől függ, hova irányítjuk a felhasználót)
export type SessionEndReason = 'logout' | 'expired'

export interface SessionState {
  session: Session | null
  endReason: SessionEndReason | null
}

const STORAGE_KEY = 'finex.session'

type Listener = () => void

const listeners = new Set<Listener>()

function isExpired(session: Session): boolean {
  return session.expiresAt !== null && Date.parse(session.expiresAt) <= Date.now()
}

// A tárolt munkamenet csak akkor érvényes, ha jó a formátuma és még nem járt le
function readStoredSession(): Session | null {
  const raw = storage.get(STORAGE_KEY)
  if (!raw) {
    return null
  }

  try {
    const parsed = JSON.parse(raw) as Partial<Session> | null
    if (!parsed || typeof parsed.token !== 'string') {
      return null
    }

    const session: Session = {
      token: parsed.token,
      expiresAt: typeof parsed.expiresAt === 'string' ? parsed.expiresAt : null,
    }
    return isExpired(session) ? null : session
  } catch {
    return null
  }
}

// A korábbi verzió csak a tokent tárolta, ezen a kulcson
storage.remove('finex.token')

let state: SessionState = { session: readStoredSession(), endReason: null }

function setState(next: SessionState) {
  state = next
  listeners.forEach((listener) => listener())
}

// Ha egy másik böngészőfülön be- vagy kilépnek, ez a fül is követi
window.addEventListener('storage', (event) => {
  if (event.key === STORAGE_KEY) {
    const session = readStoredSession()
    setState({ session, endReason: session ? null : 'logout' })
  }
})

// Külső tároló a React számára (useSyncExternalStore): a komponensek feliratkoznak a változására
export const sessionStore = {
  getState(): SessionState {
    return state
  },

  getToken(): string | null {
    return state.session?.token ?? null
  },

  start(session: Session) {
    storage.set(STORAGE_KEY, JSON.stringify(session))
    setState({ session, endReason: null })
  },

  end(reason: SessionEndReason) {
    storage.remove(STORAGE_KEY)
    setState({ session: null, endReason: reason })
  },

  subscribe(listener: Listener) {
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  },
}
