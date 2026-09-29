// localStorage-hozzáférés hibatűréssel: privát ablakban vagy letiltott tárolásnál a böngésző kivételt dob,
// ilyenkor az alkalmazás tárolás nélkül (csak a memóriában) működik tovább
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
