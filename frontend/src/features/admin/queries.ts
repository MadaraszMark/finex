import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createCategory,
  deleteCategory,
  getAdminDashboard,
  getAllAccounts,
  getAllTickets,
  getTicketAsAdmin,
  getUserDetails,
  replyToTicket,
  runJob,
  searchLoginLogs,
  searchUsers,
  updateAccountStatus,
  updateCategory,
  updateTicketStatus,
  updateUserRole,
  updateUserStatus,
  type LoginLogFilter,
} from '../../api/admin'
import { queryKeys } from '../../api/queryKeys'
import type { AccountStatus, CategoryRequest, TicketStatus, UserRole, UserStatus } from '../../api/types'

// Az admin felület lekérdezései és módosításai. Módosítás után az "admin" előtagú adatok frissülnek
// (a táblázatok, az áttekintő számai), valamint az érintett ügyféloldali adatok is.

function useAdminInvalidation(extraKeys: readonly (readonly unknown[])[] = []) {
  const queryClient = useQueryClient()
  return () => Promise.all([queryKeys.admin, ...extraKeys].map((queryKey) => queryClient.invalidateQueries({ queryKey })))
}

export function useAdminDashboard() {
  return useQuery({ queryKey: queryKeys.adminDashboard, queryFn: getAdminDashboard })
}

// Egy ütemezett feladat kézi futtatása pénzt mozgat (kamat, rendszeres átutalás): minden adat frissül
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

// A saját számlákat (ha az admin is ügyfél) és a kártyákat is érintheti (lezáráskor a kártya megszűnik)
export function useUpdateAccountStatus() {
  const invalidate = useAdminInvalidation([queryKeys.accounts, queryKeys.cards])
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

// A kategóriák a tranzakciók űrlapjain is megjelennek, ezért a közös kategórialista is frissül
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
