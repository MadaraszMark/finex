import { keepPreviousData, useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getCategories } from '../../api/categories'
import { queryKeys } from '../../api/queryKeys'
import { assignCategory, getTransaction, getTransactionCategories, removeCategory, searchTransactions } from '../../api/transactions'
import type { TransactionSearchParams } from '../../api/types'

const PAGE_SIZE = 20

// A legutóbbi tételek az összes számláról (főoldal)
export function useRecentTransactions(size: number) {
  return useQuery({
    queryKey: queryKeys.recentTransactions(size),
    queryFn: () => searchTransactions({}, 0, size),
    select: (page) => page.content,
  })
}

// Szűrőváltáskor az előző találatok halványítva látszanak, amíg az újak megjönnek
export function useTransactionSearch(filter: TransactionSearchParams) {
  return useInfiniteQuery({
    queryKey: queryKeys.transactionSearch(filter),
    queryFn: ({ pageParam }) => searchTransactions(filter, pageParam, PAGE_SIZE),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => (lastPage.page.number + 1 < lastPage.page.totalPages ? lastPage.page.number + 1 : undefined),
    placeholderData: keepPreviousData,
  })
}

export function useTransaction(id: number | undefined) {
  return useQuery({
    queryKey: queryKeys.transaction(id ?? 0),
    queryFn: () => getTransaction(id ?? 0),
    enabled: id !== undefined,
  })
}

export function useTransactionCategories(id: number | undefined) {
  return useQuery({
    queryKey: queryKeys.transactionCategories(id ?? 0),
    queryFn: () => getTransactionCategories(id ?? 0),
    enabled: id !== undefined,
  })
}

// Kategóriamódosítás után a listák és a statisztikák is frissülnek
function useCategoryChangeInvalidation() {
  const queryClient = useQueryClient()
  return () => Promise.all([queryKeys.transactions, queryKeys.statistics].map((queryKey) => queryClient.invalidateQueries({ queryKey })))
}

export function useAssignCategory() {
  const invalidate = useCategoryChangeInvalidation()
  return useMutation({
    mutationFn: ({ transactionId, categoryId }: { transactionId: number; categoryId: number }) => assignCategory(transactionId, categoryId),
    onSuccess: invalidate,
  })
}

export function useRemoveCategory() {
  const invalidate = useCategoryChangeInvalidation()
  return useMutation({ mutationFn: removeCategory, onSuccess: invalidate })
}

// A kategóriák törzsadata ritkán változik, egy munkameneten belül elég egyszer lekérni
export function useCategories() {
  return useQuery({ queryKey: queryKeys.categories, queryFn: getCategories, staleTime: Infinity })
}
