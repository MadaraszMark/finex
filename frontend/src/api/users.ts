import { api } from './client'
import type { ChangePasswordRequest, LoginLogListItem, Page, UpdateUserRequest, UserResponse } from './types'

// UserController (/users) és LoginLogController (/login-logs): a bejelentkezett felhasználó saját adatai

export async function getMe(): Promise<UserResponse> {
  const { data } = await api.get<UserResponse>('/users/me')
  return data
}

export async function updateMe(request: UpdateUserRequest): Promise<UserResponse> {
  const { data } = await api.put<UserResponse>('/users/me', request)
  return data
}

// Jelszócsere: a jelenlegi jelszót is meg kell adni (a backend ellenőrzi)
export async function changePassword(request: ChangePasswordRequest): Promise<void> {
  await api.put('/users/me/password', request)
}

// A saját belépési napló (sikeres és sikertelen próbálkozások), a legújabb elöl
export async function getMyLoginLogs(page = 0, size = 20): Promise<Page<LoginLogListItem>> {
  const { data } = await api.get<Page<LoginLogListItem>>('/login-logs/me', { params: { page, size, sort: 'createdAt,desc' } })
  return data
}
