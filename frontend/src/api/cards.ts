import { api } from './client'
import type { CardResponse } from './types'

// A bejelentkezett felhasználó összes kártyája (számlánként egy)
export async function getMyCards(): Promise<CardResponse[]> {
  const { data } = await api.get<CardResponse[]>('/cards')
  return data
}
