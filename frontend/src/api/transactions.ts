import { api } from './client'
import type { Page, TransactionListItem } from './types'

// Egy számla tranzakciói, a legújabb elöl (a backend keresővégpontja számlára szűrve)
export async function getAccountTransactions(
  accountId: number,
  page = 0,
  size = 5,
): Promise<Page<TransactionListItem>> {
  const { data } = await api.get<Page<TransactionListItem>>('/transactions', {
    params: { accountId, page, size, sort: 'createdAt,desc' },
  })
  return data
}
