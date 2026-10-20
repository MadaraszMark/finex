import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import {
  closeSavings,
  createSavings,
  depositToSavings,
  getSavings,
  getSavingsTransactions,
  updateSavings,
  withdrawFromSavings,
} from '../../api/savings'
import type { SavingsTransferRequest, UpdateSavingsAccountRequest } from '../../api/types'
import { invalidateMoneyQueries } from '../../lib/queryClient'

export function useSavings() {
  return useQuery({ queryKey: queryKeys.savings, queryFn: getSavings })
}

// A mozgások lapozva ("További mozgások")
export function useSavingsTransactions(id: number | undefined) {
  return useInfiniteQuery({
    queryKey: queryKeys.savingsTransactions(id ?? 0),
    queryFn: ({ pageParam }) => getSavingsTransactions(id ?? 0, pageParam),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.page.number + 1 < lastPage.page.totalPages ? lastPage.page.number + 1 : undefined),
    enabled: id !== undefined,
  })
}

// Nyitás (kezdő befizetéssel), befizetés, kivét és lezárás pénzmozgás a folyószámla és a megtakarítás között
export function useCreateSavings() {
  return useMutation({ mutationFn: createSavings, onSuccess: invalidateMoneyQueries })
}

export function useSavingsTransfer() {
  return useMutation({
    mutationFn: ({ id, direction, request }: { id: number; direction: 'deposit' | 'withdraw'; request: SavingsTransferRequest }) =>
      direction === 'deposit' ? depositToSavings(id, request) : withdrawFromSavings(id, request),
    onSuccess: invalidateMoneyQueries,
  })
}

export function useCloseSavings() {
  return useMutation({
    mutationFn: ({ id, accountId }: { id: number; accountId: number }) => closeSavings(id, accountId),
    onSuccess: invalidateMoneyQueries,
  })
}

// A név és a célösszeg módosítása nem mozgat pénzt, elég a megtakarítások listáját frissíteni
export function useUpdateSavings() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateSavingsAccountRequest }) => updateSavings(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.savings }),
  })
}
