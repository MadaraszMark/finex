import { api } from './client'
import type { CreateSupportTicketRequest, Page, SupportTicketListItem, SupportTicketResponse } from './types'

// SupportTicketController (/support-tickets): a bejelentkezett felhasználó ticketjei

export async function getMyTickets(page = 0, size = 50): Promise<Page<SupportTicketListItem>> {
  const { data } = await api.get<Page<SupportTicketListItem>>('/support-tickets', { params: { page, size } })
  return data
}

export async function getTicket(id: number): Promise<SupportTicketResponse> {
  const { data } = await api.get<SupportTicketResponse>(`/support-tickets/${id}`)
  return data
}

// Új ticket (egyszerre csak egy, még meg nem válaszolt ticketje lehet a felhasználónak)
export async function createTicket(request: CreateSupportTicketRequest): Promise<SupportTicketResponse> {
  const { data } = await api.post<SupportTicketResponse>('/support-tickets', request)
  return data
}

// Válasz a ticketben (lezárt ticketre nem lehet)
export async function addTicketMessage(id: number, message: string): Promise<SupportTicketResponse> {
  const { data } = await api.post<SupportTicketResponse>(`/support-tickets/${id}/messages`, { message })
  return data
}
