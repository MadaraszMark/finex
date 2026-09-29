import { useQuery } from '@tanstack/react-query'
import { getUnreadCount } from '../../api/notifications'
import { queryKeys } from '../../api/queryKeys'

// Percenként frissül, illetve amikor a felhasználó visszatér a böngészőfülre (pl. beérkező utalás értesítése)
export function useUnreadCount() {
  return useQuery({
    queryKey: queryKeys.unreadNotifications,
    queryFn: getUnreadCount,
    refetchInterval: 60 * 1000,
    refetchOnWindowFocus: true,
  })
}
