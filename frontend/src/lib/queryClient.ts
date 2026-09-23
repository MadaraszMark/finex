import { QueryClient } from '@tanstack/react-query'
import axios from 'axios'

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
