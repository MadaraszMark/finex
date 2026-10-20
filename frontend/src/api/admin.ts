import { api } from './client'
import type {
  AccountResponse,
  AccountStatus,
  AdminDashboardResponse,
  CategoryRequest,
  CategoryResponse,
  JobRunResponse,
  LoginLogResponse,
  LoginStatus,
  Page,
  SupportTicketListItem,
  SupportTicketResponse,
  TicketStatus,
  UserDetailsResponse,
  UserListItem,
  UserResponse,
  UserRole,
  UserStatus,
} from './types'

// Az adminisztrátori végpontok (/admin/**): a backend csak ADMIN szerepkörrel engedi őket

// Lapozott lista paraméterei a legújabb elemmel elöl. Azonos létrehozási időnél (pl. a demóadatoknál) az azonosító
// dönt, így egy módosítás után sem "ugrál" a sorrend. A több sort paramétert a Spring sort=…&sort=… alakban várja.
function newestFirst(params: Record<string, string | number | undefined>): URLSearchParams {
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== '') {
      search.set(key, String(value))
    }
  }
  search.append('sort', 'createdAt,desc')
  search.append('sort', 'id,desc')
  return search
}

// --- Áttekintés és ütemezett feladatok ---

export async function getAdminDashboard(): Promise<AdminDashboardResponse> {
  const { data } = await api.get<AdminDashboardResponse>('/admin/dashboard')
  return data
}

export async function runJob(job: 'savings-interest' | 'standing-orders'): Promise<JobRunResponse> {
  const { data } = await api.post<JobRunResponse>(`/admin/jobs/${job}`)
  return data
}

// --- Felhasználók ---

export async function searchUsers(search: string, page = 0, size = 20): Promise<Page<UserListItem>> {
  const { data } = await api.get<Page<UserListItem>>('/admin/users', { params: newestFirst({ search, page, size }) })
  return data
}

export async function getUserDetails(id: number): Promise<UserDetailsResponse> {
  const { data } = await api.get<UserDetailsResponse>(`/admin/users/${id}`)
  return data
}

export async function updateUserStatus(id: number, status: UserStatus): Promise<UserResponse> {
  const { data } = await api.patch<UserResponse>(`/admin/users/${id}/status`, { status })
  return data
}

export async function updateUserRole(id: number, role: UserRole): Promise<UserResponse> {
  const { data } = await api.patch<UserResponse>(`/admin/users/${id}/role`, { role })
  return data
}

// --- Számlák ---

export async function getAllAccounts(status: AccountStatus | undefined, page = 0, size = 20): Promise<Page<AccountResponse>> {
  const { data } = await api.get<Page<AccountResponse>>('/admin/accounts', { params: newestFirst({ status, page, size }) })
  return data
}

export async function updateAccountStatus(id: number, status: AccountStatus): Promise<AccountResponse> {
  const { data } = await api.patch<AccountResponse>(`/admin/accounts/${id}/status`, { status })
  return data
}

// --- Ügyfélszolgálat ---

export async function getAllTickets(status: TicketStatus | undefined, page = 0, size = 50): Promise<Page<SupportTicketListItem>> {
  const { data } = await api.get<Page<SupportTicketListItem>>('/admin/support-tickets', { params: { status, page, size } })
  return data
}

export async function getTicketAsAdmin(id: number): Promise<SupportTicketResponse> {
  const { data } = await api.get<SupportTicketResponse>(`/admin/support-tickets/${id}`)
  return data
}

// Az ügyfélszolgálat válasza (a ticket "Folyamatban" állapotba kerül, az ügyfél értesítést kap)
export async function replyToTicket(id: number, message: string): Promise<SupportTicketResponse> {
  const { data } = await api.post<SupportTicketResponse>(`/admin/support-tickets/${id}/messages`, { message })
  return data
}

export async function updateTicketStatus(id: number, status: TicketStatus): Promise<SupportTicketResponse> {
  const { data } = await api.patch<SupportTicketResponse>(`/admin/support-tickets/${id}/status`, { status })
  return data
}

// --- Belépési napló ---

export interface LoginLogFilter {
  status?: LoginStatus
  userId?: number
  ipAddress?: string
  from?: string
  to?: string
}

export async function searchLoginLogs(filter: LoginLogFilter, page = 0, size = 30): Promise<Page<LoginLogResponse>> {
  const { data } = await api.get<Page<LoginLogResponse>>('/admin/login-logs', { params: { ...filter, page, size } })
  return data
}

// --- Kategóriák ---

export async function createCategory(request: CategoryRequest): Promise<CategoryResponse> {
  const { data } = await api.post<CategoryResponse>('/admin/categories', request)
  return data
}

export async function updateCategory(id: number, request: CategoryRequest): Promise<CategoryResponse> {
  const { data } = await api.put<CategoryResponse>(`/admin/categories/${id}`, request)
  return data
}

// Használatban lévő kategória nem törölhető (a backend hibát ad)
export async function deleteCategory(id: number): Promise<void> {
  await api.delete(`/admin/categories/${id}`)
}
