import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../api/queryKeys'
import { getBankSettings } from '../api/settings'

// A bank üzleti szabályai a backendből (pl. napi utalási limit, legfeljebb hány számla nyitható). Futás közben nem
// változnak, ezért egyszer töltődnek be; amíg nincsenek meg, a felület nem állít róluk semmit (a backend úgyis ellenőriz).
export function useBankSettings() {
  return useQuery({ queryKey: queryKeys.settings, queryFn: getBankSettings, staleTime: Infinity })
}
