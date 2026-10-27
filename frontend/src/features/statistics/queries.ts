import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import { getCategorySpending, getMonthlySummary, getSummary, getTopExpenses } from '../../api/statistics'

// Az előző adat csak azonos pénznemnél (kulcs 3. eleme) marad, különben rossz jellel látszana
function keepPreviousOfSameCurrency<T>(currency: string | undefined) {
  return (previous: T | undefined, previousQuery?: { queryKey: readonly unknown[] }) => (previousQuery?.queryKey[2] === currency ? previous : undefined)
}

export function useSummary(currency: string | undefined) {
  return useQuery({
    queryKey: queryKeys.summary(currency ?? ''),
    queryFn: () => getSummary(currency ?? 'HUF'),
    enabled: currency !== undefined,
    placeholderData: keepPreviousOfSameCurrency(currency),
  })
}

export function useMonthlySummary(currency: string | undefined, months = 6) {
  return useQuery({
    queryKey: queryKeys.monthlySummary(currency ?? '', months),
    queryFn: () => getMonthlySummary(months, currency ?? 'HUF'),
    enabled: currency !== undefined,
    placeholderData: keepPreviousOfSameCurrency(currency),
  })
}

export function useCategorySpending(currency: string | undefined, from: string, to: string) {
  return useQuery({
    queryKey: queryKeys.categorySpending(currency ?? '', from, to),
    queryFn: () => getCategorySpending(from, to, currency ?? 'HUF'),
    enabled: currency !== undefined,
    placeholderData: keepPreviousOfSameCurrency(currency),
  })
}

// A legnagyobb kiadásokat a backend szűri (saját számlák közti mozgás és sztornó nem számít)
export function useTopExpenses(currency: string | undefined, from: string, to: string, count = 5) {
  return useQuery({
    queryKey: queryKeys.topExpenses(currency ?? '', from, to),
    queryFn: () => getTopExpenses(from, to, currency ?? 'HUF', count),
    enabled: currency !== undefined,
    placeholderData: keepPreviousOfSameCurrency(currency),
  })
}
