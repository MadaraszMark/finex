import { api } from './client'
import type { CardPaymentRequest, CardResponse, TransactionResponse, UpdateCardLimitsRequest } from './types'

// CardController (/cards): a bejelentkezett felhasználó bankkártyái (számlánként egy)

export async function getMyCards(): Promise<CardResponse[]> {
  const { data } = await api.get<CardResponse[]>('/cards')
  return data
}

// Ideiglenes tiltás: a kártyával addig nem lehet fizetni, amíg a felhasználó fel nem oldja
export async function blockCard(id: number): Promise<CardResponse> {
  const { data } = await api.patch<CardResponse>(`/cards/${id}/block`)
  return data
}

export async function unblockCard(id: number): Promise<CardResponse> {
  const { data } = await api.patch<CardResponse>(`/cards/${id}/unblock`)
  return data
}

// Napi költési limit és az online fizetés engedélyezése (a kettő együtt módosul)
export async function updateCardLimits(id: number, request: UpdateCardLimitsRequest): Promise<CardResponse> {
  const { data } = await api.put<CardResponse>(`/cards/${id}/limits`, request)
  return data
}

// Demó kártyás fizetés: a számla egyenlegéből levonódik, és beleszámít a mai költésbe
export async function payWithCard(id: number, request: CardPaymentRequest): Promise<TransactionResponse> {
  const { data } = await api.post<TransactionResponse>(`/cards/${id}/payments`, request)
  return data
}
