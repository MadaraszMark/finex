import { api } from './client'
import type { BankSettingsResponse } from './types'

// BankSettingsController (/settings): az üzleti szabályok (napi limit, max. számlaszám, …)
export async function getBankSettings(): Promise<BankSettingsResponse> {
  const { data } = await api.get<BankSettingsResponse>('/settings')
  return data
}
