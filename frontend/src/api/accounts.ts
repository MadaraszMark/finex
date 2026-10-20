import { api } from './client'
import type { AccountResponse, BalanceHistoryItem, CreateAccountRequest, DepositRequest, StatementResponse } from './types'

// AccountController (/accounts) és BalanceHistoryController (/balance-history): a bejelentkezett felhasználó számlái

// Az összes számla (a lezártak is), nyitás szerinti sorrendben
export async function getMyAccounts(): Promise<AccountResponse[]> {
  const { data } = await api.get<AccountResponse[]>('/accounts')
  return data
}

export async function getAccount(id: number): Promise<AccountResponse> {
  const { data } = await api.get<AccountResponse>(`/accounts/${id}`)
  return data
}

// Új számla nyitása (a backend bankkártyát is ad hozzá)
export async function openAccount(request: CreateAccountRequest): Promise<AccountResponse> {
  const { data } = await api.post<AccountResponse>('/accounts', request)
  return data
}

// Csak nulla egyenlegű, nem utolsó nyitott számla zárható le; a hozzá tartozó kártya is megszűnik
export async function closeAccount(id: number): Promise<void> {
  await api.delete(`/accounts/${id}`)
}

// Demó készpénzbefizetés (pl. ATM-en)
export async function deposit(accountId: number, request: DepositRequest): Promise<AccountResponse> {
  const { data } = await api.post<AccountResponse>(`/accounts/${accountId}/deposit`, request)
  return data
}

// Számlakivonat futó egyenleggel; a from és a to "ÉÉÉÉ-HH-NN", mindkét nap beleszámít
export async function getStatement(accountId: number, from: string, to: string): Promise<StatementResponse> {
  const { data } = await api.get<StatementResponse>(`/accounts/${accountId}/statement`, { params: { from, to } })
  return data
}

// Az egyenleg alakulása egy időszakban (minden könyvelt tétel utáni egyenleg, időrendben)
export async function getBalanceHistory(accountId: number, from: string, to: string): Promise<BalanceHistoryItem[]> {
  const { data } = await api.get<BalanceHistoryItem[]>(`/balance-history/account/${accountId}`, { params: { from, to } })
  return data
}
