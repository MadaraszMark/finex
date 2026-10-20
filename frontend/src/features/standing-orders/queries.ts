import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import { cancelStandingOrder, createStandingOrder, getStandingOrders, updateStandingOrder } from '../../api/standingOrders'
import type { UpdateStandingOrderRequest } from '../../api/types'

export function useStandingOrders() {
  return useQuery({ queryKey: queryKeys.standingOrders, queryFn: getStandingOrders })
}

function useStandingOrderInvalidation() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: queryKeys.standingOrders })
}

export function useCreateStandingOrder() {
  const invalidate = useStandingOrderInvalidation()
  return useMutation({ mutationFn: createStandingOrder, onSuccess: invalidate })
}

export function useUpdateStandingOrder() {
  const invalidate = useStandingOrderInvalidation()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateStandingOrderRequest }) => updateStandingOrder(id, request),
    onSuccess: invalidate,
  })
}

export function useCancelStandingOrder() {
  const invalidate = useStandingOrderInvalidation()
  return useMutation({ mutationFn: cancelStandingOrder, onSuccess: invalidate })
}
