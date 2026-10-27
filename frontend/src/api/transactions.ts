import { api } from './client'
import type {
  ExchangeRequest,
  ExchangeResponse,
  Page,
  TransactionCategoryLink,
  TransactionListItem,
  TransactionResponse,
  TransactionSearchParams,
  TransferRequest,
  TransferResponse,
} from './types'

// TransactionController (/transactions) és TransactionCategoryController (/transaction-categories)

// Keresés a felhasználó összes számlájának tételei között, alapból a legújabb elöl
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

// Utalás; az Idempotency-Key miatt a kétszer elküldött utalás csak egyszer könyvelődik
export async function transfer({ request, idempotencyKey }: { request: TransferRequest; idempotencyKey: string }): Promise<TransferResponse> {
  const { data } = await api.post<TransferResponse>('/transactions/transfer', request, { headers: { 'Idempotency-Key': idempotencyKey } })
  return data
}

// Devizaváltás két saját számla között a bank árfolyamán (szintén Idempotency-Key-jel)
export async function exchange({ request, idempotencyKey }: { request: ExchangeRequest; idempotencyKey: string }): Promise<ExchangeResponse> {
  const { data } = await api.post<ExchangeResponse>('/transactions/exchange', request, { headers: { 'Idempotency-Key': idempotencyKey } })
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
