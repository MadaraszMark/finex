import { QueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { queryKeys } from '../api/queryKeys'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // 4xx hibánál (pl. hiányzó jogosultság) felesleges újrapróbálkozni, egyébként egyszer próbálja újra
      retry: (failureCount, error) => {
        const isClientError = axios.isAxiosError(error) && (error.response?.status ?? 500) < 500
        return !isClientError && failureCount < 1
      },
      refetchOnWindowFocus: false,
    },
  },
})

// Pénzmozgás után minden érintett adat frissül (egyenlegek, kártyák, tételek, statisztikák, …)
export function invalidateMoneyQueries() {
  return Promise.all(
    [queryKeys.accounts, queryKeys.cards, queryKeys.transactions, queryKeys.savings, queryKeys.statistics, queryKeys.notifications].map((queryKey) =>
      queryClient.invalidateQueries({ queryKey }),
    ),
  )
}
