import axios from 'axios'
import { authStore } from '../auth/authStore'
import { endSession } from '../auth/session'
import type { ApiError } from './types'

// Minden backend-hívás ezen a példányon megy át. A /api előtagú kéréseket
// fejlesztés közben a Vite proxy továbbítja a Spring Boot backendnek (vite.config.ts).
export const api = axios.create({ baseURL: '/api' })

// A bejelentkezéskor kapott JWT token minden kérés fejlécébe bekerül
api.interceptors.request.use((config) => {
  const token = authStore.getToken()
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

// Lejárt vagy érvénytelen tokennél (401) a munkamenet véget ér, és a felhasználó a belépő oldalra kerül
api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      endSession()
    }
    return Promise.reject(error)
  },
)

// Felhasználónak megjeleníthető hibaüzenet a backend ApiError válaszából
export function getErrorMessage(error: unknown): string {
  if (!axios.isAxiosError<ApiError>(error)) {
    return 'Ismeretlen hiba történt.'
  }
  if (!error.response) {
    return 'A szerver nem érhető el.'
  }

  const { status, data } = error.response
  // Ha a Vite proxy nem éri el a backendet, üres 500-as választ ad
  if (!data || typeof data !== 'object') {
    return status >= 500
      ? 'A backend nem érhető el. Fut a Spring Boot alkalmazás?'
      : `Hiba történt (HTTP ${status}).`
  }
  if (data.violations?.length) {
    return data.violations.map((violation) => violation.message).join(' ')
  }
  return data.message || `Hiba történt (HTTP ${status}).`
}
