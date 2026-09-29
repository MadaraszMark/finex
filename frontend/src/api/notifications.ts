import { api } from './client'
import type { UnreadCountResponse } from './types'

// NotificationController (/notifications)

// Olvasatlan értesítések száma (a fejléc csengőjének jelvényéhez)
export async function getUnreadCount(): Promise<number> {
  const { data } = await api.get<UnreadCountResponse>('/notifications/unread-count')
  return data.count
}
