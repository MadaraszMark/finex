import { storage } from '../lib/storage'

// A felhasználó választása, illetve a ténylegesen megjelenített téma
export type ThemePreference = 'light' | 'dark' | 'system'
export type ResolvedTheme = 'light' | 'dark'

export interface ThemeState {
  preference: ThemePreference
  resolved: ResolvedTheme
}

const STORAGE_KEY = 'finex.theme'

// A mobilböngésző címsorának színe témánként (az index.css --background értékei)
const THEME_COLORS: Record<ResolvedTheme, string> = {
  light: '#f7f5fc',
  dark: '#0d0a16',
}

const systemDark = window.matchMedia('(prefers-color-scheme: dark)')

type Listener = () => void

const listeners = new Set<Listener>()

function readPreference(): ThemePreference {
  const stored = storage.get(STORAGE_KEY)
  return stored === 'light' || stored === 'dark' ? stored : 'system'
}

function resolve(preference: ThemePreference): ResolvedTheme {
  if (preference === 'system') {
    return systemDark.matches ? 'dark' : 'light'
  }
  return preference
}

// A <html> "dark" osztálya kapcsolja a sötét színeket (index.css). Váltás közben az átmenetek ki vannak kapcsolva,
// két képkocka múlva visszakapcsolnak.
function applyToDocument(resolved: ResolvedTheme) {
  const root = document.documentElement
  root.classList.add('theme-switching')
  root.classList.toggle('dark', resolved === 'dark')
  root.style.colorScheme = resolved
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', THEME_COLORS[resolved])
  requestAnimationFrame(() => requestAnimationFrame(() => root.classList.remove('theme-switching')))
}

function createState(preference: ThemePreference): ThemeState {
  return { preference, resolved: resolve(preference) }
}

let state = createState(readPreference())
applyToDocument(state.resolved)

function update(preference: ThemePreference) {
  state = createState(preference)
  applyToDocument(state.resolved)
  listeners.forEach((listener) => listener())
}

// "Rendszer" beállításnál követi az operációs rendszer váltását (pl. esti automatikus sötét mód)
systemDark.addEventListener('change', () => {
  if (state.preference === 'system') {
    update('system')
  }
})

// Egy másik böngészőfülön végzett váltást is követi
window.addEventListener('storage', (event) => {
  if (event.key === STORAGE_KEY) {
    update(readPreference())
  }
})

export const themeStore = {
  getState(): ThemeState {
    return state
  },

  setPreference(preference: ThemePreference) {
    if (preference === 'system') {
      storage.remove(STORAGE_KEY)
    } else {
      storage.set(STORAGE_KEY, preference)
    }
    update(preference)
  },

  subscribe(listener: Listener) {
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  },
}
