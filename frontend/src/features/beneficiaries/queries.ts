import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createBeneficiary, deleteBeneficiary, getBeneficiaries, updateBeneficiary } from '../../api/beneficiaries'
import { queryKeys } from '../../api/queryKeys'
import type { BeneficiaryRequest } from '../../api/types'

export function useBeneficiaries() {
  return useQuery({ queryKey: queryKeys.beneficiaries, queryFn: getBeneficiaries })
}

function useBeneficiaryInvalidation() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: queryKeys.beneficiaries })
}

export function useCreateBeneficiary() {
  const invalidate = useBeneficiaryInvalidation()
  return useMutation({ mutationFn: createBeneficiary, onSuccess: invalidate })
}

export function useUpdateBeneficiary() {
  const invalidate = useBeneficiaryInvalidation()
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: BeneficiaryRequest }) => updateBeneficiary(id, request),
    onSuccess: invalidate,
  })
}

export function useDeleteBeneficiary() {
  const invalidate = useBeneficiaryInvalidation()
  return useMutation({ mutationFn: deleteBeneficiary, onSuccess: invalidate })
}
