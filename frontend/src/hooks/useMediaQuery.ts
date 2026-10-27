import { useSyncExternalStore } from 'react'

// Egy CSS média-lekérdezés aktuális állapota, villanás nélkül (useSyncExternalStore)
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
