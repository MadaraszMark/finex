// A backend DTO-inak (hu.finex.main.dto) megfelelő típusok.
// A BigDecimal összegek számként, az Instant időpontok ISO-szövegként, a LocalDate dátumok "ÉÉÉÉ-HH-NN" alakban érkeznek.

export type TransactionType = 'INCOME' | 'OUTCOME' | 'TRANSFER_IN' | 'TRANSFER_OUT'
export type AccountStatus = 'ACTIVE' | 'BLOCKED' | 'CLOSED' | 'FROZEN'
export type AccountType = 'CURRENT' | 'SAVINGS' | 'CREDIT'
export type CardStatus = 'ACTIVE' | 'BLOCKED' | 'CANCELLED'
export type UserRole = 'USER' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'BLOCKED'
export type NotificationType = 'TRANSACTION' | 'SECURITY' | 'SAVINGS' | 'SUPPORT' | 'SYSTEM'

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

// Regisztráció (CreateUserRequest): a szerepkör mindig USER, azt a backend állítja be
export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  phone?: string
  password: string
}

export interface UserResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  phone: string | null
  role: UserRole
  status: UserStatus
  createdAt: string
  updatedAt: string
}

export interface AuthResponse {
  token: string
  expiresAt: string
  user: UserResponse
}

export interface AccountResponse {
  id: number
  userId: number
  name: string
  accountNumber: string
  balance: number
  currency: string
  accountType: AccountType
  status: AccountStatus
  createdAt: string
}

export interface DepositRequest {
  amount: number
  message?: string
}

// A teljes kártyaszám soha nem jön le, csak a maszkolt alak ("**** **** **** 1184")
export interface CardResponse {
  id: number
  accountId: number
  accountNumber: string
  maskedNumber: string
  holderName: string
  expiryDate: string
  status: CardStatus
  dailyLimit: number
  spentToday: number
  onlinePaymentEnabled: boolean
  createdAt: string
}

export interface CategoryResponse {
  id: number
  name: string
  icon: string | null
}

export interface TransactionListItem {
  id: number
  accountId: number
  type: TransactionType
  amount: number
  message: string | null
  partnerName: string | null
  currency: string
  categories: CategoryResponse[]
  createdAt: string
}

export interface NotificationResponse {
  id: number
  type: NotificationType
  title: string
  message: string
  read: boolean
  createdAt: string
}

export interface UnreadCountResponse {
  count: number
}

// Spring Data lapozott válasz (a backend VIA_DTO módban küldi: a lapozási adatok a "page" objektumban vannak)
export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}
