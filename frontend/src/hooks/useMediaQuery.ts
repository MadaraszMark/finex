import { useSyncExternalStore } from 'react'

// Egy CSS média-lekérdezés aktuális állapota (pl. "(min-width: 640px)"), átméretezéskor frissül.
// A useSyncExternalStore miatt renderelés közben is megbízhatóan olvasható, nincs villanás.
export function useMediaQuery(query: string): boolean {
  return useSyncExternalStore(
    (onChange) => {
      const media = window.matchMedia(query)
      media.addEventListener('change', onChange)
      return () => media.removeEventListener('change', onChange)
    },
    () => window.matchMedia(query).matches,
    () => false,
  )
}
