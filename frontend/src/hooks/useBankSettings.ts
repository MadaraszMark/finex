import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../api/queryKeys'
import { getBankSettings } from '../api/settings'

// A bank üzleti szabályai a backendből; futás közben nem változnak, ezért csak egyszer töltődnek be
export function useBankSettings() {
  return useQuery({ queryKey: queryKeys.settings, queryFn: getBankSettings, staleTime: Infinity })
}
