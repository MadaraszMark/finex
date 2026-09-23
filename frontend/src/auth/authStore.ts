// A JWT token tárolója: localStorage-ban marad meg oldalfrissítés után is,
// a React komponensek pedig a useAuthToken hookkal iratkoznak fel a változására.
const TOKEN_KEY = 'finex.token'

type Listener = () => void

const listeners = new Set<Listener>()

function readStoredToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

let token = readStoredToken()

function notifyListeners() {
  listeners.forEach((listener) => listener())
}

// Ha egy másik böngészőfülön be- vagy kilépnek, ez a fül is követi
window.addEventListener('storage', (event) => {
  if (event.key === TOKEN_KEY) {
    token = event.newValue
    notifyListeners()
  }
})

export const authStore = {
  getToken(): string | null {
    return token
  },

  setToken(newToken: string) {
    token = newToken
    try {
      localStorage.setItem(TOKEN_KEY, newToken)
    } catch {
      // tárolás nélkül a token csak a memóriában marad meg
    }
    notifyListeners()
  },

  clear() {
    token = null
    try {
      localStorage.removeItem(TOKEN_KEY)
    } catch {
      // nincs mit törölni
    }
    notifyListeners()
  },

  subscribe(listener: Listener) {
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  },
}
