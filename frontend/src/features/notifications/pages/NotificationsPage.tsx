import { BellOff, CheckCheck, ChevronDown } from 'lucide-react'
import { motion } from 'motion/react'
import { useState } from 'react'
import { getErrorMessage } from '../../../api/client'
import type { NotificationResponse, NotificationType } from '../../../api/types'
import Button from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import ChipGroup from '../../../components/ui/ChipGroup'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { formatDayHeading } from '../../../lib/format'
import { staggerContainer, staggerItem } from '../../../lib/motion'
import NotificationItem from '../components/NotificationItem'
import { NotificationRowsSkeleton } from '../components/NotificationsSkeleton'
import { notificationMeta } from '../notificationMeta'
import { useMarkAllAsRead, useNotifications, useUnreadCount } from '../queries'
import { useOpenNotification } from '../useOpenNotification'

type Filter = 'ALL' | 'UNREAD' | NotificationType

const filterOptions: { value: Filter; label: string }[] = [
  { value: 'ALL', label: 'Összes' },
  { value: 'UNREAD', label: 'Olvasatlan' },
  ...(Object.keys(notificationMeta) as NotificationType[]).map((type) => ({ value: type, label: notificationMeta[type].label })),
]

function groupByDay(items: NotificationResponse[], now: Date) {
  const groups: { key: string; heading: string; items: NotificationResponse[] }[] = []
  for (const item of items) {
    const date = new Date(item.createdAt)
    const key = date.toDateString()
    let group = groups.at(-1)
    if (!group || group.key !== key) {
      group = { key, heading: formatDayHeading(date, now), items: [] }
      groups.push(group)
    }
    group.items.push(item)
  }
  return groups
}

// Értesítések: napok szerint, típus és olvasottság szerinti szűréssel; egyenként kattintásra, vagy egyszerre
// ("Mind olvasott") jelölhetők olvasottnak
export default function NotificationsPage() {
  const notificationsQuery = useNotifications()
  const { data: unread = 0 } = useUnreadCount()
  const markAllAsRead = useMarkAllAsRead()
  const openNotification = useOpenNotification()
  const [filter, setFilter] = useState<Filter>('ALL')
  const [now] = useState(() => new Date())

  const all = notificationsQuery.data?.pages.flatMap((page) => page.content) ?? []
  const visible = all.filter((item) => (filter === 'ALL' ? true : filter === 'UNREAD' ? !item.read : item.type === filter))

  return (
    <>
      <PageHeader
        title="Értesítések"
        description={unread > 0 ? `${unread} olvasatlan értesítésed van.` : 'Minden fontos esemény egy helyen.'}
        actions={
          unread > 0 && (
            <Button variant="glass" leftIcon={<CheckCheck />} loading={markAllAsRead.isPending} onClick={() => markAllAsRead.mutate()}>
              Mind olvasott
            </Button>
          )
        }
      />

      <Card className="p-5 sm:p-6">
        <ChipGroup label="Szűrés" value={filter} options={filterOptions} onChange={setFilter} />

        <div className="mt-5">
          {notificationsQuery.isPending ? (
            <NotificationRowsSkeleton count={5} />
          ) : notificationsQuery.isError ? (
            <QueryError message={getErrorMessage(notificationsQuery.error)} retrying={notificationsQuery.isFetching} onRetry={() => void notificationsQuery.refetch()} />
          ) : visible.length === 0 ? (
            <EmptyState
              icon={<BellOff />}
              title={filter === 'ALL' ? 'Még nincs értesítésed' : 'Nincs ilyen értesítés'}
              description="A beérkező utalások, biztonsági események és ügyfélszolgálati válaszok itt jelennek meg."
            />
          ) : (
            <motion.div variants={staggerContainer} initial="hidden" animate="show">
              {groupByDay(visible, now).map((group) => (
                <section key={group.key} aria-label={group.heading} className="mt-5 first:mt-0">
                  <h2 className="px-3 text-xs font-bold tracking-wider text-muted-foreground uppercase">{group.heading}</h2>
                  <ul className="mt-2 space-y-1">
                    {group.items.map((notification) => (
                      <motion.li key={notification.id} variants={staggerItem}>
                        <NotificationItem notification={notification} now={now} onOpen={openNotification} />
                      </motion.li>
                    ))}
                  </ul>
                </section>
              ))}
            </motion.div>
          )}

          {notificationsQuery.hasNextPage && (
            <div className="mt-6 flex justify-center">
              <Button variant="outline" rightIcon={<ChevronDown />} loading={notificationsQuery.isFetchingNextPage} onClick={() => void notificationsQuery.fetchNextPage()}>
                Korábbi értesítések
              </Button>
            </div>
          )}
        </div>
      </Card>
    </>
  )
}
