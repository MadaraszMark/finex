import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import { changePassword, getMyLoginLogs, logoutEverywhere, updateMe } from '../../api/users'
import { replaceToken } from '../../session/session'

// A "me" gyorsítótár azonnal frissül; az e-mail-cseréről biztonsági értesítés is keletkezik
export function useUpdateProfile() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: updateMe,
    onSuccess: (user) => {
      queryClient.setQueryData(queryKeys.me, user)
      void queryClient.invalidateQueries({ queryKey: queryKeys.notifications })
    },
  })
}

// Jelszócsere után a régi token érvénytelen: a kapott új tokennel marad belépve ez a munkamenet
export function useChangePassword() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: changePassword,
    onSuccess: (auth) => {
      replaceToken(auth)
      void queryClient.invalidateQueries({ queryKey: queryKeys.notifications })
    },
  })
}

// Kijelentkezés minden más eszközről (pl. elveszett telefon): ez a munkamenet új tokent kap
export function useLogoutEverywhere() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: logoutEverywhere,
    onSuccess: (auth) => {
      replaceToken(auth)
      void queryClient.invalidateQueries({ queryKey: queryKeys.notifications })
    },
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
