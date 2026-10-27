import { Navigate, Outlet, useLocation, type Location } from 'react-router'
import { AdminRouteSkeleton } from '../features/admin/components/AdminSkeletons'
import ForbiddenPage from '../features/errors/ForbiddenPage'
import { useCurrentUser } from './useCurrentUser'
import { useSessionState } from './useSession'

// Útvonalvédelem csak a felületen; a valódi jogosultság-ellenőrzés a backendben van

interface RedirectState {
  from?: Location
}

// Csak bejelentkezve; lejárt munkamenet után a belépés oda visz vissza, ahol járt
export function RequireAuth() {
  const { session, endReason } = useSessionState()
  const location = useLocation()

  if (session) {
    return <Outlet />
  }
  if (endReason === 'logout') {
    return <Navigate to="/login" replace />
  }
  if (location.pathname === '/' && endReason === null) {
    return <Navigate to="/welcome" replace />
  }
  return <Navigate to="/login" replace state={{ from: location } satisfies RedirectState} />
}

// Csak kijelentkezve; belépés után innen irányít tovább
export function GuestOnly() {
  const { session } = useSessionState()
  const location = useLocation()

  if (session) {
    const from = (location.state as RedirectState | null)?.from
    const target = from ? `${from.pathname}${from.search}${from.hash}` : '/'
    return <Navigate to={target} replace />
  }
  return <Outlet />
}

// Csak ADMIN szerepkörrel; amíg a GET /users/me betöltődik, az admin oldal váza látszik
export function RequireAdmin() {
  const { data: user, isPending, isError } = useCurrentUser()
  const location = useLocation()

  if (isPending) {
    return <AdminRouteSkeleton pathname={location.pathname} />
  }
  if (isError || user.role !== 'ADMIN') {
    return <ForbiddenPage />
  }
  return <Outlet />
}
