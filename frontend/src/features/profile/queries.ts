import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import { changePassword, getMyLoginLogs, updateMe } from '../../api/users'

// A módosított adatok rögtön megjelennek a fejlécben, az oldalsávban és a menüben is (a "me" gyorsítótárban)
export function useUpdateProfile() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: updateMe,
    onSuccess: (user) => queryClient.setQueryData(queryKeys.me, user),
  })
}

// A jelszócseréről biztonsági értesítés is keletkezhet, ezért az értesítések frissülnek
export function useChangePassword() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: changePassword,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.notifications }),
  })
}

export function useMyLoginLogs() {
  return useInfiniteQuery({
    queryKey: queryKeys.myLoginLogs,
    queryFn: ({ pageParam }) => getMyLoginLogs(pageParam, 10),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.page.number + 1 < lastPage.page.totalPages ? lastPage.page.number + 1 : undefined),
  })
}
