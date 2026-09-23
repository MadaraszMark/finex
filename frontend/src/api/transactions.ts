import { api } from './client'
import type { Page, TransactionListItem } from './types'

// Egy számla tranzakciói, a legújabb elöl
export async function getAccountTransactions(
  accountId: number,
  page = 0,
  size = 5,
): Promise<Page<TransactionListItem>> {
  const { data } = await api.get<Page<TransactionListItem>>(`/transactions/account/${accountId}`, {
    params: { page, size },
  })
  return data
}
