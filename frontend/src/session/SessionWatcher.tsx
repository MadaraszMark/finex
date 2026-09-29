import { useEffect } from 'react'
import { endSession } from './session'
import { useSession } from './useSession'

// A böngésző időzítője legfeljebb ~24,8 napot tud várni
const MAX_TIMEOUT_MS = 2_147_483_647

// A token lejáratakor (AuthResponse.expiresAt) automatikusan kilépteti a felhasználót,
// nem kell megvárni, hogy a következő kérés 401-et kapjon
export default function SessionWatcher() {
  const session = useSession()

  useEffect(() => {
    if (!session?.expiresAt) {
      return
    }

    const remaining = Date.parse(session.expiresAt) - Date.now()
    const timer = window.setTimeout(() => endSession('expired'), Math.min(Math.max(remaining, 0), MAX_TIMEOUT_MS))

    return () => window.clearTimeout(timer)
  }, [session])

  return null
}
