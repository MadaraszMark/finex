import { api } from './client'
import type { AuthResponse, LoginRequest, RegisterRequest } from './types'

// AuthController (/auth): nyilvános végpontok, a válaszban JWT token és a felhasználó adatai

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/login', request)
  return data
}

// Regisztráció után rögtön bejelentkezik (a backend folyószámlát és kártyát is nyit)
export async function register(request: RegisterRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/register', request)
  return data
}
