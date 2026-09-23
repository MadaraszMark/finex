import { Navigate, Outlet } from 'react-router'
import { useAuthToken } from '../auth/useAuthToken'

// Csak bejelentkezett felhasználó láthatja a benne lévő oldalakat, különben a belépő oldalra kerül
export default function ProtectedRoute() {
  const token = useAuthToken()

  if (!token) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}
