import { useSyncExternalStore } from 'react'
import { themeStore, type ThemeState } from './themeStore'

// Az aktuális téma; újrarajzol, ha a felhasználó vagy az operációs rendszer témát vált
export function useTheme(): ThemeState {
  return useSyncExternalStore(themeStore.subscribe, themeStore.getState)
}
