import { api } from './client'
import type {
  CreateSavingsAccountRequest,
  Page,
  SavingsAccountResponse,
  SavingsTransactionResponse,
  SavingsTransferRequest,
  SavingsTransferResponse,
  UpdateSavingsAccountRequest,
} from './types'

// SavingsAccountController (/savings): a bejelentkezett felhasználó megtakarítási céljai

// A nem lezárt megtakarítások, nyitás szerinti sorrendben
export async function getSavings(): Promise<SavingsAccountResponse[]> {
  const { data } = await api.get<SavingsAccountResponse[]>('/savings')
  return data
}

// Új megtakarítás; a kezdő összeg a megadott folyószámláról kerül át (a pénznem is az övé lesz)
export async function createSavings(request: CreateSavingsAccountRequest): Promise<SavingsAccountResponse> {
  const { data } = await api.post<SavingsAccountResponse>('/savings', request)
  return data
}

// Név és célösszeg módosítása (célösszeg nélkül a cél nélküli gyűjtés marad)
export async function updateSavings(id: number, request: UpdateSavingsAccountRequest): Promise<SavingsAccountResponse> {
  const { data } = await api.put<SavingsAccountResponse>(`/savings/${id}`, request)
  return data
}

export async function depositToSavings(id: number, request: SavingsTransferRequest): Promise<SavingsTransferResponse> {
  const { data } = await api.post<SavingsTransferResponse>(`/savings/${id}/deposit-from-account`, request)
  return data
}

export async function withdrawFromSavings(id: number, request: SavingsTransferRequest): Promise<SavingsTransferResponse> {
  const { data } = await api.post<SavingsTransferResponse>(`/savings/${id}/withdraw-to-account`, request)
  return data
}

// A megtakarítás mozgásai (befizetés, kivét, kamat), a legújabb elöl
export async function getSavingsTransactions(id: number, page = 0, size = 20): Promise<Page<SavingsTransactionResponse>> {
  const { data } = await api.get<Page<SavingsTransactionResponse>>(`/savings/${id}/transactions`, {
    params: { page, size, sort: 'createdAt,desc' },
  })
  return data
}

// Lezárás: a teljes egyenleg a megadott folyószámlára kerül
export async function closeSavings(id: number, accountId: number): Promise<void> {
  await api.delete(`/savings/${id}`, { params: { accountId } })
}
