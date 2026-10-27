import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createCategory,
  deleteCategory,
  depositToAccount,
  getAccountTransactions,
  getAdminDashboard,
  getAllAccounts,
  getAllTickets,
  getReconciliation,
  getTicketAsAdmin,
  getUserDetails,
  replyToTicket,
  reverseTransaction,
  runJob,
  searchAuditLog,
  searchLoginLogs,
  searchUsers,
  updateAccountStatus,
  updateCategory,
  updateTicketStatus,
  updateUserRole,
  updateUserStatus,
  type AuditLogFilter,
  type LoginLogFilter,
} from '../../api/admin'
import { queryKeys } from '../../api/queryKeys'
import type { AccountStatus, CategoryRequest, DepositRequest, TicketStatus, UserRole, UserStatus } from '../../api/types'
import { invalidateMoneyQueries } from '../../lib/queryClient'

// Módosítás után az "admin" előtagú és az érintett ügyféloldali adatok is frissülnek

function useAdminInvalidation(extraKeys: readonly (readonly unknown[])[] = []) {
  const queryClient = useQueryClient()
  return () => Promise.all([queryKeys.admin, ...extraKeys].map((queryKey) => queryClient.invalidateQueries({ queryKey })))
}

export function useAdminDashboard() {
  return useQuery({ queryKey: queryKeys.adminDashboard, queryFn: getAdminDashboard })
}

// Az ütemezett feladat pénzt mozgat (kamat, rendszeres átutalás): minden adat frissül
export function useRunJob() {
  const queryClient = useQueryClient()
  return useMutation({ mutationFn: runJob, onSuccess: () => queryClient.invalidateQueries() })
}

export function useAdminUsers(search: string, page: number) {
  return useQuery({ queryKey: queryKeys.adminUsers(search, page), queryFn: () => searchUsers(search, page), placeholderData: keepPreviousData })
}

export function useAdminUser(id: number | undefined) {
  return useQuery({ queryKey: queryKeys.adminUser(id ?? 0), queryFn: () => getUserDetails(id ?? 0), enabled: id !== undefined })
}

export function useUpdateUserStatus() {
  const invalidate = useAdminInvalidation()
  return useMutation({ mutationFn: ({ id, status }: { id: number; status: UserStatus }) => updateUserStatus(id, status), onSuccess: invalidate })
}

export function useUpdateUserRole() {
  const invalidate = useAdminInvalidation()
  return useMutation({ mutationFn: ({ id, role }: { id: number; role: UserRole }) => updateUserRole(id, role), onSuccess: invalidate })
}

export function useAdminAccounts(status: AccountStatus | undefined, page: number) {
  return useQuery({
    queryKey: queryKeys.adminAccounts(status ?? 'ALL', page),
    queryFn: () => getAllAccounts(status, page),
    placeholderData: keepPreviousData,
  })
}

// Lezáráskor a kártya megszűnik, a rendszeres átutalások leállnak, az ügyfél értesítést kap
export function useUpdateAccountStatus() {
  const invalidate = useAdminInvalidation([queryKeys.accounts, queryKeys.cards, queryKeys.standingOrders, queryKeys.notifications])
  return useMutation({ mutationFn: ({ id, status }: { id: number; status: AccountStatus }) => updateAccountStatus(id, status), onSuccess: invalidate })
}

export function useAdminTickets(status: TicketStatus | undefined) {
  return useQuery({
    queryKey: queryKeys.adminTickets(status ?? 'ALL'),
    queryFn: () => getAllTickets(status),
    select: (page) => page.content,
    placeholderData: keepPreviousData,
  })
}

export function useAdminTicket(id: number | undefined) {
  return useQuery({
    queryKey: queryKeys.adminTicket(id ?? 0),
    queryFn: () => getTicketAsAdmin(id ?? 0),
    enabled: id !== undefined,
    refetchInterval: 30_000,
  })
}

export function useReplyToTicket() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, message }: { id: number; message: string }) => replyToTicket(id, message),
    onSuccess: (ticket) => {
      queryClient.setQueryData(queryKeys.adminTicket(ticket.id), ticket)
      return queryClient.invalidateQueries({ queryKey: queryKeys.admin })
    },
  })
}

export function useUpdateTicketStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: TicketStatus }) => updateTicketStatus(id, status),
    onSuccess: (ticket) => {
      queryClient.setQueryData(queryKeys.adminTicket(ticket.id), ticket)
      return queryClient.invalidateQueries({ queryKey: queryKeys.admin })
    },
  })
}

export function useAdminLoginLogs(filter: LoginLogFilter, page: number) {
  return useQuery({
    queryKey: queryKeys.adminLoginLogs(filter, page),
    queryFn: () => searchLoginLogs(filter, page),
    placeholderData: keepPreviousData,
  })
}

// A közös kategórialista is frissül (a tranzakciók űrlapjain is megjelenik)
export function useSaveCategory() {
  const invalidate = useAdminInvalidation([queryKeys.categories])
  return useMutation({
    mutationFn: ({ id, request }: { id?: number; request: CategoryRequest }) => (id === undefined ? createCategory(request) : updateCategory(id, request)),
    onSuccess: invalidate,
  })
}

export function useDeleteCategory() {
  const invalidate = useAdminInvalidation([queryKeys.categories])
  return useMutation({ mutationFn: deleteCategory, onSuccess: invalidate })
}

// Befizetés és sztornó után az ügyfél pénzügyi adatai is frissülnek
export function useAdminDeposit() {
  const invalidate = useAdminInvalidation()
  return useMutation({
    mutationFn: ({ accountId, request }: { accountId: number; request: DepositRequest }) => depositToAccount(accountId, request),
    onSuccess: () => Promise.all([invalidate(), invalidateMoneyQueries()]),
  })
}

export function useAdminAccountTransactions(accountId: number | undefined, page: number) {
  return useQuery({
    queryKey: queryKeys.adminAccountTransactions(accountId ?? 0, page),
    queryFn: () => getAccountTransactions(accountId ?? 0, page),
    enabled: accountId !== undefined,
    placeholderData: keepPreviousData,
  })
}

export function useReverseTransaction() {
  const invalidate = useAdminInvalidation()
  return useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) => reverseTransaction(id, reason),
    onSuccess: () => Promise.all([invalidate(), invalidateMoneyQueries()]),
  })
}

export function useReconciliation() {
  return useQuery({ queryKey: queryKeys.adminReconciliation, queryFn: getReconciliation })
}

export function useAuditLog(filter: AuditLogFilter, page: number) {
  return useQuery({
    queryKey: queryKeys.adminAuditLog(filter, page),
    queryFn: () => searchAuditLog(filter, page),
    placeholderData: keepPreviousData,
  })
}
