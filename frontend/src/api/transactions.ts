import { api } from './client'
import type { Page, TransactionCategoryLink, TransactionListItem, TransactionResponse, TransactionSearchParams, TransferRequest, TransferResponse } from './types'

// TransactionController (/transactions) és TransactionCategoryController (/transaction-categories)

// A bejelentkezett felhasználó összes számlájának tételei között keres, alapból a legújabb elöl
// (a statisztika a legnagyobb összegűeket kéri: sort = "amount,desc")
export async function searchTransactions(filter: TransactionSearchParams, page = 0, size = 20, sort = 'createdAt,desc'): Promise<Page<TransactionListItem>> {
  const { data } = await api.get<Page<TransactionListItem>>('/transactions', {
    params: { ...filter, page, size, sort },
  })
  return data
}

export async function getTransaction(id: number): Promise<TransactionResponse> {
  const { data } = await api.get<TransactionResponse>(`/transactions/${id}`)
  return data
}

// Utalás egy saját számláról; a backend ellenőrzi az IBAN-t, a fedezetet, a napi limitet és a devizanemet
export async function transfer(request: TransferRequest): Promise<TransferResponse> {
  const { data } = await api.post<TransferResponse>('/transactions/transfer', request)
  return data
}

// Egy tétel kategóriái a kapcsolat azonosítójával (ezzel lehet levenni egy kategóriát)
export async function getTransactionCategories(transactionId: number): Promise<TransactionCategoryLink[]> {
  const { data } = await api.get<TransactionCategoryLink[]>(`/transaction-categories/transaction/${transactionId}`)
  return data
}

export async function assignCategory(transactionId: number, categoryId: number): Promise<void> {
  await api.post(`/transaction-categories/${transactionId}/assign/${categoryId}`)
}

export async function removeCategory(linkId: number): Promise<void> {
  await api.delete(`/transaction-categories/${linkId}`)
}
