import type { NotificationResponse } from '../../../api/types'
import { cn } from '../../../lib/cn'
import { formatRelativeTime } from '../../../lib/format'
import { notificationMeta } from '../notificationMeta'

interface NotificationItemProps {
  notification: NotificationResponse
  now: Date
  onOpen: (notification: NotificationResponse) => void
  // A csengő lenyíló listájában az üzenet legfeljebb két sor
  compact?: boolean
}

// Egy értesítés; kattintásra olvasottá válik, és az esemény részleteihez visz
export default function NotificationItem({ notification, now, onOpen, compact = false }: NotificationItemProps) {
  const meta = notificationMeta[notification.type]

  return (
    <button
      type="button"
      onClick={() => onOpen(notification)}
      className={cn(
        'flex w-full items-start gap-3.5 rounded-2xl p-3 text-left transition hover:bg-muted/70 focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none',
        !notification.read && 'bg-brand-50/80 dark:bg-brand-950/40',
      )}
    >
      <span className={cn('flex size-10 shrink-0 items-center justify-center rounded-xl', meta.badge)}>
        <meta.icon aria-hidden className="size-5" />
      </span>
      <span className="min-w-0 flex-1">
        <span className="flex items-center gap-2">
          <span className={cn('truncate text-sm text-foreground', notification.read ? 'font-semibold' : 'font-extrabold')}>{notification.title}</span>
          {!notification.read && (
            <span className="size-2 shrink-0 rounded-full bg-accent shadow-[0_0_0_3px_rgb(250_204_21/0.25)]">
              <span className="sr-only">(olvasatlan)</span>
            </span>
          )}
        </span>
        <span className={cn('mt-0.5 block text-sm text-muted-foreground', compact && 'line-clamp-2')}>{notification.message}</span>
        <span className="mt-1 block text-xs text-muted-foreground">
          {meta.label} · <time dateTime={notification.createdAt}>{formatRelativeTime(notification.createdAt, now)}</time>
        </span>
      </span>
    </button>
  )
}
