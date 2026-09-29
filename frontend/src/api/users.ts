import { api } from './client'
import type { UserResponse } from './types'

// UserController (/users): a bejelentkezett felhasználó saját adatai

export async function getMe(): Promise<UserResponse> {
  const { data } = await api.get<UserResponse>('/users/me')
  return data
}
