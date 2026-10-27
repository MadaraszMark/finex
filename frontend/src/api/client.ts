import axios from 'axios'
import { endSession } from '../session/session'
import { sessionStore } from '../session/sessionStore'
import type { ApiError } from './types'

// Minden backend-hívás ezen a példányon megy át (a /api kéréseket a Vite proxy továbbítja)
export const api = axios.create({ baseURL: '/api' })

// A bejelentkezéskor kapott JWT token minden kérés fejlécébe bekerül
api.interceptors.request.use((config) => {
  const token = sessionStore.getToken()
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

// 401: lejárt token vagy letiltott felhasználó, a munkamenet véget ér
api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      endSession('expired')
    }
    return Promise.reject(error)
  },
)

function getApiError(error: unknown): ApiError | null {
  if (!axios.isAxiosError<ApiError>(error) || !error.response) {
    return null
  }
  const { data } = error.response
  return data && typeof data === 'object' ? data : null
}

// Felhasználónak megjeleníthető hibaüzenet a backend ApiError válaszából (GlobalExceptionHandler)
export function getErrorMessage(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return 'Ismeretlen hiba történt.'
  }
  if (!error.response) {
    return 'A szerver nem érhető el. Ellenőrizd az internetkapcsolatot.'
  }

  const apiError = getApiError(error)
  // Ha a Vite proxy nem éri el a backendet, üres 500-as választ ad
  if (!apiError) {
    return error.response.status >= 500
      ? 'A szerver jelenleg nem érhető el. Próbáld újra később.'
      : `Hiba történt (HTTP ${error.response.status}).`
  }
  if (apiError.violations?.length) {
    return apiError.violations.map((violation) => violation.message).join(' ')
  }
  return apiError.message || `Hiba történt (HTTP ${error.response.status}).`
}

// 404: nem létezik vagy nem a felhasználóé (a backend szándékosan nem különbözteti meg)
export function isNotFoundError(error: unknown): boolean {
  return axios.isAxiosError(error) && error.response?.status === 404
}

// A backend validációs hibái mezőnként, az űrlapmezőkhöz
export function getFieldErrors(error: unknown): Record<string, string> {
  const violations = getApiError(error)?.violations ?? []
  return Object.fromEntries(violations.map((violation) => [violation.field, violation.message]))
}
