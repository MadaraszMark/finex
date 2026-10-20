import { api } from './client'
import type { BankSettingsResponse } from './types'

// BankSettingsController (/settings): az üzleti szabályok paraméterei (napi utalási limit, nyitható számlák száma, …)
export async function getBankSettings(): Promise<BankSettingsResponse> {
  const { data } = await api.get<BankSettingsResponse>('/settings')
  return data
}
