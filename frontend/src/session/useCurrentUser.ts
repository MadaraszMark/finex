import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../api/queryKeys'
import { getMe } from '../api/users'
import { useSession } from './useSession'

// A bejelentkezett felhasználó adatai (GET /users/me). Belépéskor a válaszból már a gyorsítótárban vannak.
export function useCurrentUser() {
  const session = useSession()

  return useQuery({
    queryKey: queryKeys.me,
    queryFn: getMe,
    enabled: session !== null,
    staleTime: 5 * 60 * 1000,
  })
}
