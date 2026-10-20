import { ArrowRight, Bell, BellOff, CheckCheck } from 'lucide-react'
import { AnimatePresence, motion } from 'motion/react'
import { useState } from 'react'
import { Link } from 'react-router'
import { iconButtonStyles } from '../../components/ui/buttonStyles'
import Popover from '../../components/ui/Popover'
import Skeleton from '../../components/ui/Skeleton'
import NotificationItem from './components/NotificationItem'
import { useLatestNotifications, useMarkAllAsRead, useUnreadCount } from './queries'
import { useOpenNotification } from './useOpenNotification'

// A csengő lenyíló panelje: a legfrissebb értesítések, "Mind olvasott" és az összes értesítés oldala
function BellPanel({ close, unread }: { close: () => void; unread: number }) {
  const latestQuery = useLatestNotifications(true)
  const markAllAsRead = useMarkAllAsRead()
  const openNotification = useOpenNotification(close)
  const [now] = useState(() => new Date())

  return (
    <div>
      <div className="flex items-center justify-between gap-3 px-3 pt-2 pb-2">
        <p className="font-bold text-foreground">Értesítések</p>
        {unread > 0 && (
          <button
            type="button"
            disabled={markAllAsRead.isPending}
            onClick={() => markAllAsRead.mutate()}
            className="inline-flex items-center gap-1.5 rounded-lg px-2 py-1 text-sm font-semibold text-primary transition hover:bg-muted focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
          >
            <CheckCheck className="size-4" />
            Mind olvasott
          </button>
        )}
      </div>

      <div className="max-h-[60vh] overflow-y-auto">
        {latestQuery.isPending ? (
          <div className="space-y-2 p-2">
            {[0, 1, 2].map((index) => (
              <Skeleton key={index} className="h-16" />
            ))}
          </div>
        ) : !latestQuery.data || latestQuery.data.length === 0 ? (
          <p className="flex flex-col items-center gap-2 px-4 py-8 text-center text-sm text-muted-foreground">
            <BellOff aria-hidden className="size-6" />
            Nincs új értesítésed.
          </p>
        ) : (
          <ul className="space-y-1">
            {latestQuery.data.map((notification) => (
              <li key={notification.id}>
                <NotificationItem notification={notification} now={now} onOpen={openNotification} compact />
              </li>
            ))}
          </ul>
        )}
      </div>

      <Link
        to="/notifications"
        onClick={close}
        className="group mt-2 flex items-center justify-center gap-1.5 rounded-2xl border-t border-border px-3 pt-3 pb-1.5 text-sm font-semibold text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
      >
        Összes értesítés
        <ArrowRight aria-hidden className="size-4 transition-transform group-hover:translate-x-0.5" />
      </Link>
    </div>
  )
}

// A koncepcióképek csengője (a lila felső sávon üveghatású): olvasatlan értesítésnél sárga jelvény mutatja a számukat,
// kattintásra lenyílik a legfrissebb értesítések listája
export default function NotificationBell() {
  const { data: count = 0 } = useUnreadCount()

  return (
    <Popover
      label={count > 0 ? `Értesítések, ${count} olvasatlan` : 'Értesítések'}
      triggerClassName={iconButtonStyles('light')}
      panelClassName="w-[22rem] max-w-[calc(100vw-2rem)]"
      trigger={
        <>
          <Bell />
          <AnimatePresence>
            {count > 0 && (
              <motion.span
                key="badge"
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                exit={{ scale: 0 }}
                transition={{ type: 'spring', bounce: 0.5, duration: 0.4 }}
                className="absolute -top-1.5 -right-1.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1 text-[10px] font-bold text-brand-950 shadow-md shadow-brand-950/30"
              >
                {count > 9 ? '9+' : count}
              </motion.span>
            )}
          </AnimatePresence>
        </>
      }
    >
      {(close) => <BellPanel close={close} unread={count} />}
    </Popover>
  )
}
