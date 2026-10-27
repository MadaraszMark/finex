import { useMutation } from '@tanstack/react-query'
import { transfer } from '../../api/transactions'
import { invalidateMoneyQueries } from '../../lib/queryClient'

// Az utalás pénzmozgás: egyenlegek, tételek, statisztikák és értesítések frissülnek
export function useTransfer() {
  return useMutation({ mutationFn: transfer, onSuccess: invalidateMoneyQueries })
}
