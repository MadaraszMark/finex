import { api } from './client'
import type {
  AccountResponse,
  AccountStatus,
  AdminDashboardResponse,
  AuditAction,
  AuditLogResponse,
  CategoryRequest,
  CategoryResponse,
  DepositRequest,
  JobRunResponse,
  LoginLogResponse,
  LoginStatus,
  Page,
  ReconciliationResponse,
  SupportTicketListItem,
  SupportTicketResponse,
  TicketStatus,
  TransactionListItem,
  TransactionResponse,
  UserDetailsResponse,
  UserListItem,
  UserResponse,
  UserRole,
  UserStatus,
} from './types'

// Az adminisztrátori végpontok (/admin/**): a backend csak ADMIN szerepkörrel engedi őket

// Legújabb elöl; azonos létrehozási időnél az id dönt, így a sorrend nem ugrál
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

export async function getAdminDashboard(): Promise<AdminDashboardResponse> {
  const { data } = await api.get<AdminDashboardResponse>('/admin/dashboard')
  return data
}

export async function runJob(job: 'savings-interest' | 'standing-orders'): Promise<JobRunResponse> {
  const { data } = await api.post<JobRunResponse>(`/admin/jobs/${job}`)
  return data
}

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

export async function getAllAccounts(status: AccountStatus | undefined, page = 0, size = 20): Promise<Page<AccountResponse>> {
  const { data } = await api.get<Page<AccountResponse>>('/admin/accounts', { params: newestFirst({ status, page, size }) })
  return data
}

export async function updateAccountStatus(id: number, status: AccountStatus): Promise<AccountResponse> {
  const { data } = await api.patch<AccountResponse>(`/admin/accounts/${id}/status`, { status })
  return data
}

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

// Készpénzbefizetés egy ügyfél számlájára (jóváírást csak az admin rögzíthet)
export async function depositToAccount(accountId: number, request: DepositRequest): Promise<AccountResponse> {
  const { data } = await api.post<AccountResponse>(`/admin/accounts/${accountId}/deposit`, request)
  return data
}

// Egy számla tételei (a sztornóhoz), legfrissebb elöl
export async function getAccountTransactions(accountId: number, page = 0, size = 10): Promise<Page<TransactionListItem>> {
  const { data } = await api.get<Page<TransactionListItem>>(`/admin/accounts/${accountId}/transactions`, { params: { page, size } })
  return data
}

// Sztornó: az eredeti tétel megmarad, egy ellentétes irányú javító tétel semlegesíti
export async function reverseTransaction(id: number, reason: string): Promise<TransactionResponse> {
  const { data } = await api.post<TransactionResponse>(`/admin/transactions/${id}/reverse`, { reason })
  return data
}

// Főkönyvi egyeztetés: a tárolt egyenlegek megegyeznek-e a könyvelt tételekkel
export async function getReconciliation(): Promise<ReconciliationResponse> {
  const { data } = await api.get<ReconciliationResponse>('/admin/reconciliation')
  return data
}

export interface AuditLogFilter {
  action?: AuditAction
  actorId?: number
  from?: string
  to?: string
}

export async function searchAuditLog(filter: AuditLogFilter, page = 0, size = 30): Promise<Page<AuditLogResponse>> {
  const { data } = await api.get<Page<AuditLogResponse>>('/admin/audit-log', { params: { ...filter, page, size } })
  return data
}
