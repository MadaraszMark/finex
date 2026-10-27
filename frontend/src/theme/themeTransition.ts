import { themeStore, type ThemePreference } from './themeStore'

// Táguló körös témaváltás (View Transitions API); támogatás vagy mozgás nélkül azonnali
export function changeTheme(preference: ThemePreference, origin?: { x: number; y: number }) {
  const apply = () => themeStore.setPreference(preference)
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

  if (typeof document.startViewTransition !== 'function' || reduceMotion || themeStore.getState().preference === preference) {
    apply()
    return
  }

  const x = origin?.x ?? window.innerWidth / 2
  const y = origin?.y ?? 0
  // A kör sugara a kattintás helyétől a legtávolabbi sarokig ér
  const radius = Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y))

  const transition = document.startViewTransition(apply)
  void transition.ready.then(() => {
    document.documentElement.animate(
      { clipPath: [`circle(0px at ${x}px ${y}px)`, `circle(${radius}px at ${x}px ${y}px)`] },
      { duration: 600, easing: 'cubic-bezier(0.22, 1, 0.36, 1)', pseudoElement: '::view-transition-new(root)' },
    )
  })
}

// Egy elem középpontja (a témaváltó gomb, ahonnan a kör indul)
export function elementCenter(element: Element | null): { x: number; y: number } | undefined {
  if (!element) {
    return undefined
  }
  const rect = element.getBoundingClientRect()
  return { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 }
}
