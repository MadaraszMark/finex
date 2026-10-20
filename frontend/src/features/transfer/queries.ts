import { useMutation } from '@tanstack/react-query'
import { transfer } from '../../api/transactions'
import { invalidateMoneyQueries } from '../../lib/queryClient'

// Az utalás pénzmozgás: az egyenlegek, a tranzakciók, a statisztikák és az értesítések is frissülnek
export function useTransfer() {
  return useMutation({ mutationFn: transfer, onSuccess: invalidateMoneyQueries })
}
