import { useInfiniteQuery, useMutation, useQuery, useQueryClient, type InfiniteData } from '@tanstack/react-query'
import { getNotifications, getUnreadCount, markAllAsRead, markAsRead } from '../../api/notifications'
import { queryKeys } from '../../api/queryKeys'
import type { NotificationResponse, Page } from '../../api/types'

// Percenként frissül, és amikor a felhasználó visszatér a böngészőfülre
export function useUnreadCount() {
  return useQuery({
    queryKey: queryKeys.unreadNotifications,
    queryFn: getUnreadCount,
    refetchInterval: 60 * 1000,
    refetchOnWindowFocus: true,
  })
}

export function useNotifications() {
  return useInfiniteQuery({
    queryKey: queryKeys.notificationList,
    queryFn: ({ pageParam }) => getNotifications(pageParam),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.page.number + 1 < lastPage.page.totalPages ? lastPage.page.number + 1 : undefined),
  })
}

export function useLatestNotifications(enabled: boolean) {
  return useQuery({
    queryKey: [...queryKeys.notifications, 'latest'],
    queryFn: () => getNotifications(0, 6),
    select: (page) => page.content,
    enabled,
  })
}

// A "notifications" előtag alatt: jelvényszám, csengőlista és lapozott lista
type NotificationCache = number | Page<NotificationResponse> | InfiniteData<Page<NotificationResponse>> | undefined

// A gyorsítótár olvasottra állítva, mintha a szerver már megtette volna
function withRead(data: NotificationCache, target: number | 'all'): NotificationCache {
  if (data === undefined) {
    return data
  }
  if (typeof data === 'number') {
    return target === 'all' ? 0 : Math.max(data - 1, 0)
  }
  const markPage = (page: Page<NotificationResponse>) => ({
    ...page,
    content: page.content.map((notification) => (target === 'all' || notification.id === target ? { ...notification, read: true } : notification)),
  })
  return 'pages' in data ? { ...data, pages: data.pages.map(markPage) } : markPage(data)
}

// Optimista frissítés: azonnal olvasott, hiba esetén visszaáll az előző állapot
function useOptimisticRead<T>(mutationFn: (variables: T) => Promise<unknown>, target: (variables: T) => number | 'all') {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onMutate: async (variables) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.notifications })
      const snapshot = queryClient.getQueriesData<NotificationCache>({ queryKey: queryKeys.notifications })
      queryClient.setQueriesData<NotificationCache>({ queryKey: queryKeys.notifications }, (data) => withRead(data, target(variables)))
      return { snapshot }
    },
    onError: (_error, _variables, context) => context?.snapshot.forEach(([queryKey, data]) => queryClient.setQueryData(queryKey, data)),
    onSettled: () => queryClient.invalidateQueries({ queryKey: queryKeys.notifications }),
  })
}

export function useMarkAsRead() {
  return useOptimisticRead(markAsRead, (id: number) => id)
}

export function useMarkAllAsRead() {
  return useOptimisticRead<void>(markAllAsRead, () => 'all')
}
