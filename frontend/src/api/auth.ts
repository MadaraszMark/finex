import { api } from './client'
import type { AuthResponse, LoginRequest, RegisterRequest } from './types'

// AuthController (/auth): nyilvános végpontok, a válaszban a JWT token és a felhasználó adatai vannak

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/login', request)
  return data
}

// Regisztráció után a felhasználó rögtön be is van jelentkezve (a backend folyószámlát és kártyát is nyit neki)
export async function register(request: RegisterRequest): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/register', request)
  return data
}
