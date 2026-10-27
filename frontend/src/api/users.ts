import { api } from './client'
import type { AuthResponse, ChangePasswordRequest, LoginLogListItem, Page, UpdateUserRequest, UserResponse } from './types'

// UserController és LoginLogController: a bejelentkezett felhasználó saját adatai

export async function getMe(): Promise<UserResponse> {
  const { data } = await api.get<UserResponse>('/users/me')
  return data
}

export async function updateMe(request: UpdateUserRequest): Promise<UserResponse> {
  const { data } = await api.put<UserResponse>('/users/me', request)
  return data
}

// Jelszócsere: a többi eszköz tokenje érvénytelen lesz, ez a munkamenet új tokent kap
export async function changePassword(request: ChangePasswordRequest): Promise<AuthResponse> {
  const { data } = await api.put<AuthResponse>('/users/me/password', request)
  return data
}

// Kijelentkezés minden más eszközről: a régi tokenek érvénytelenek, ez a munkamenet újat kap
export async function logoutEverywhere(): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/users/me/logout-all')
  return data
}

// A saját belépési napló (sikeres és sikertelen próbálkozások), a legújabb elöl
export async function getMyLoginLogs(page = 0, size = 20): Promise<Page<LoginLogListItem>> {
  const { data } = await api.get<Page<LoginLogListItem>>('/login-logs/me', { params: { page, size, sort: 'createdAt,desc' } })
  return data
}
