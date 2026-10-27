// Hibatűrő localStorage: privát ablakban kivételt dobhat, ilyenkor tárolás nélkül megy tovább
export const storage = {
  get(key: string): string | null {
    try {
      return localStorage.getItem(key)
    } catch {
      return null
    }
  },

  set(key: string, value: string) {
    try {
      localStorage.setItem(key, value)
    } catch {
      // tárolás nélkül az érték csak a memóriában marad meg
    }
  },

  remove(key: string) {
    try {
      localStorage.removeItem(key)
    } catch {
      // nincs mit törölni
    }
  },
}
