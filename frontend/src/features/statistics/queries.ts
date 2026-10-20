import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import { getCategorySpending, getMonthlySummary, getSummary } from '../../api/statistics'
import { searchTransactions } from '../../api/transactions'
import { isIncoming } from '../../lib/format'

// Szűrőváltáskor az előző adatok látszanak (halványítva), amíg az újak megérkeznek: nincs ugrálás, nincs villanás

export function useSummary(currency: string | undefined) {
  return useQuery({
    queryKey: queryKeys.summary(currency ?? ''),
    queryFn: () => getSummary(currency ?? 'HUF'),
    enabled: currency !== undefined,
    placeholderData: keepPreviousData,
  })
}

export function useMonthlySummary(currency: string | undefined, months = 6) {
  return useQuery({
    queryKey: queryKeys.monthlySummary(currency ?? '', months),
    queryFn: () => getMonthlySummary(months, currency ?? 'HUF'),
    enabled: currency !== undefined,
    placeholderData: keepPreviousData,
  })
}

export function useCategorySpending(currency: string | undefined, from: string, to: string) {
  return useQuery({
    queryKey: queryKeys.categorySpending(currency ?? '', from, to),
    queryFn: () => getCategorySpending(from, to, currency ?? 'HUF'),
    enabled: currency !== undefined,
    placeholderData: keepPreviousData,
  })
}

// Az időszak legnagyobb kiadásai: a backend összeg szerint rendezve adja a tételeket (bevételt is), ebből
// maradnak a kimenő, adott pénznemű tételek
export function useTopExpenses(currency: string | undefined, from: string, to: string, count = 5) {
  return useQuery({
    queryKey: queryKeys.topExpenses(currency ?? '', from, to),
    queryFn: () => searchTransactions({ from, to }, 0, 50, 'amount,desc'),
    enabled: currency !== undefined,
    placeholderData: keepPreviousData,
    select: (page) => page.content.filter((item) => !isIncoming(item.type) && item.currency === currency).slice(0, count),
  })
}
