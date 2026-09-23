import { api } from './client'
import type { AuthResponse, LoginRequest, UserResponse } from './types'

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/login', request)
  return data
}

export async function getMe(): Promise<UserResponse> {
  const { data } = await api.get<UserResponse>('/users/me')
  return data
}
