import { useNavigate } from 'react-router'
import type { NotificationResponse } from '../../api/types'
import { notificationMeta } from './notificationMeta'
import { useMarkAsRead } from './queries'

// Megnyitás: olvasottá teszi (ha még nem az), és az esemény részleteihez navigál
export function useOpenNotification(onNavigate?: () => void) {
  const navigate = useNavigate()
  const markAsRead = useMarkAsRead()

  return (notification: NotificationResponse) => {
    if (!notification.read) {
      markAsRead.mutate(notification.id)
    }
    const link = notificationMeta[notification.type].link
    if (link) {
      onNavigate?.()
      navigate(link)
    }
  }
}
