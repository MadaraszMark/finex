import { Navigate, Outlet, useLocation, type Location } from 'react-router'
import PageLoader from '../components/ui/PageLoader'
import ForbiddenPage from '../features/errors/ForbiddenPage'
import { useCurrentUser } from './useCurrentUser'
import { useSessionState } from './useSession'

// Útvonalvédelem: a router ezekbe csomagolja a védett, illetve csak vendégeknek szóló oldalakat.
// A valódi jogosultság-ellenőrzés a backendben van, ez csak a felületet irányítja a megfelelő helyre.

interface RedirectState {
  from?: Location
}

// Csak bejelentkezve. Kilépés után a belépő oldalra, új látogatót a főoldalról a nyitóképernyőre visz;
// lejárt munkamenetnél megjegyzi, hol járt a felhasználó, és belépés után oda viszi vissza.
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

// Csak kijelentkezve (nyitóképernyő, belépés, regisztráció). Belépés után innen irányít tovább.
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

// Csak ADMIN szerepkörrel (a szerepkört a GET /users/me válaszából ismerjük)
export function RequireAdmin() {
  const { data: user, isPending, isError } = useCurrentUser()

  if (isPending) {
    return <PageLoader />
  }
  if (isError || user.role !== 'ADMIN') {
    return <ForbiddenPage />
  }
  return <Outlet />
}
