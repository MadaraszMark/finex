import { api } from './client'
import type { AccountResponse, DepositRequest } from './types'

// A bejelentkezett felhasználó aktív folyószámlája
export async function getMyAccount(): Promise<AccountResponse> {
  const { data } = await api.get<AccountResponse>('/accounts/me')
  return data
}

export async function deposit(accountId: number, request: DepositRequest): Promise<AccountResponse> {
  const { data } = await api.post<AccountResponse>(`/accounts/${accountId}/deposit`, request)
  return data
}
