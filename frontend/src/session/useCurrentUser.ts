import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../api/queryKeys'
import { getMe } from '../api/users'
import { useSession } from './useSession'

// A bejelentkezett felhasználó (GET /users/me); belépéskor már a gyorsítótárban van
export function useCurrentUser() {
  const session = useSession()

  return useQuery({
    queryKey: queryKeys.me,
    queryFn: getMe,
    enabled: session !== null,
    staleTime: 5 * 60 * 1000,
  })
}
