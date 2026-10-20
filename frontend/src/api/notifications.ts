import { api } from './client'
import type { NotificationResponse, Page, UnreadCountResponse } from './types'

// NotificationController (/notifications)

// Olvasatlan értesítések száma (a fejléc csengőjének jelvényéhez)
export async function getUnreadCount(): Promise<number> {
  const { data } = await api.get<UnreadCountResponse>('/notifications/unread-count')
  return data.count
}

// Az értesítések lapozva, a legújabb elöl
export async function getNotifications(page = 0, size = 20): Promise<Page<NotificationResponse>> {
  const { data } = await api.get<Page<NotificationResponse>>('/notifications', { params: { page, size, sort: 'createdAt,desc' } })
  return data
}

export async function markAsRead(id: number): Promise<NotificationResponse> {
  const { data } = await api.patch<NotificationResponse>(`/notifications/${id}/read`)
  return data
}

export async function markAllAsRead(): Promise<void> {
  await api.patch('/notifications/read-all')
}
