// A backend DTO-inak (hu.finex.main.dto) megfelelő típusok.
// A BigDecimal összegek számként, az Instant időpontok ISO-szövegként érkeznek.

export type TransactionType = 'INCOME' | 'OUTCOME' | 'TRANSFER_IN' | 'TRANSFER_OUT'
export type AccountStatus = 'ACTIVE' | 'BLOCKED' | 'CLOSED' | 'FROZEN'
export type AccountType = 'CURRENT' | 'SAVINGS' | 'CREDIT'

// A GlobalExceptionHandler hibaválasza
export interface ApiError {
  timestamp: string
  status: number
  message: string
  violations?: { field: string; message: string }[] | null
}

export interface LoginRequest {
  email: string
  password: string
}

export interface UserResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  phone: string | null
  role: string
  createdAt: string
  updatedAt: string
}

export interface AuthResponse {
  token: string
  user: UserResponse
}

export interface AccountResponse {
  id: number
  userId: number
  accountNumber: string
  balance: number
  currency: string
  accountType: AccountType
  cardNumber: string | null
  status: AccountStatus
  createdAt: string
}

export interface DepositRequest {
  amount: number
  message?: string
}

export interface TransactionListItem {
  id: number
  type: TransactionType
  amount: number
  message: string | null
  currency: string
  createdAt: string
}

// Spring Data lapozott válasz – csak a felhasznált mezők
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
