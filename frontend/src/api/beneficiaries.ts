import { api } from './client'
import type { BeneficiaryRequest, BeneficiaryResponse } from './types'

// BeneficiaryController (/beneficiaries): a felhasználó kedvezményezettjei, név szerint

export async function getBeneficiaries(): Promise<BeneficiaryResponse[]> {
  const { data } = await api.get<BeneficiaryResponse[]>('/beneficiaries')
  return data
}

export async function createBeneficiary(request: BeneficiaryRequest): Promise<BeneficiaryResponse> {
  const { data } = await api.post<BeneficiaryResponse>('/beneficiaries', request)
  return data
}

export async function updateBeneficiary(id: number, request: BeneficiaryRequest): Promise<BeneficiaryResponse> {
  const { data } = await api.put<BeneficiaryResponse>(`/beneficiaries/${id}`, request)
  return data
}

export async function deleteBeneficiary(id: number): Promise<void> {
  await api.delete(`/beneficiaries/${id}`)
}
