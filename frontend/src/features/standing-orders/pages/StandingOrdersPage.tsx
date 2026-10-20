import { Archive, Ban, CalendarClock, ChevronDown, Pencil, Plus } from 'lucide-react'
import { motion } from 'motion/react'
import { useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse, StandingOrderResponse } from '../../../api/types'
import Badge from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import { cardSurface } from '../../../components/ui/cardStyles'
import ConfirmDialog from '../../../components/ui/ConfirmDialog'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { cn } from '../../../lib/cn'
import { currencyName, formatDaysUntil, formatMoney, formatShortDate, formatShortIban, frequencyLabels, parseIsoDate } from '../../../lib/format'
import { staggerContainer, staggerItem } from '../../../lib/motion'
import { useAccounts } from '../../accounts/queries'
import { useBeneficiaries } from '../../beneficiaries/queries'
import StandingOrderDialog from '../components/StandingOrderDialog'
import StandingOrdersSkeleton from '../components/StandingOrdersSkeleton'
import { useCancelStandingOrder, useStandingOrders } from '../queries'

const nextDateFormatter = new Intl.DateTimeFormat('hu-HU', { month: 'short', day: 'numeric', weekday: 'long' })

// Az aktív megbízások havi terhe pénznemenként (a heti megbízás egy hónapra átszámítva: 52 hét / 12 hónap)
function monthlyTotals(orders: StandingOrderResponse[]) {
  const totals = new Map<string, number>()
  for (const order of orders) {
    const monthly = order.frequency === 'WEEKLY' ? (order.amount * 52) / 12 : order.amount
    totals.set(order.currency, (totals.get(order.currency) ?? 0) + monthly)
  }
  return [...totals.entries()]
}

interface OrderCardProps {
  order: StandingOrderResponse
  account: AccountResponse | undefined
  now: Date
  onEdit?: () => void
  onCancel?: () => void
}

function OrderCard({ order, account, now, onEdit, onCancel }: OrderCardProps) {
  return (
    <div className={cn('flex flex-col gap-4 p-5 sm:p-6', !order.active && 'opacity-70')}>
      <div className="flex flex-wrap items-start gap-4">
        <span className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300">
          <CalendarClock aria-hidden className="size-5" />
        </span>
        <div className="min-w-0 flex-1">
          <p className="truncate text-lg font-bold text-foreground">{order.partnerName}</p>
          <p className="text-sm text-muted-foreground">
            {frequencyLabels[order.frequency]} · {account?.name ?? formatShortIban(order.accountNumber)} →{' '}
            <span className="font-mono">{formatShortIban(order.toAccountNumber)}</span>
          </p>
          {order.message && <p className="mt-0.5 truncate text-sm text-muted-foreground">„{order.message}”</p>}
        </div>
        <div className="flex flex-col items-end gap-1.5">
          <p className="text-xl font-extrabold tracking-tight text-foreground">{formatMoney(order.amount, order.currency)}</p>
          {!order.active && <Badge tone="neutral">Megszüntetve</Badge>}
        </div>
      </div>

      <div className="flex flex-col gap-1 rounded-2xl bg-muted/70 px-4 py-3 text-sm sm:flex-row sm:items-center sm:justify-between">
        {order.active ? (
          <span className="text-foreground">
            Következő: <span className="font-semibold">{nextDateFormatter.format(parseIsoDate(order.nextExecutionDate))}</span>{' '}
            <span className="text-muted-foreground">({formatDaysUntil(order.nextExecutionDate, now)})</span>
          </span>
        ) : (
          <span className="text-muted-foreground">Nem teljesül többé</span>
        )}
        <span className="text-muted-foreground">
          Utoljára: {order.lastExecutionAt ? formatShortDate(new Date(order.lastExecutionAt), now) : 'még nem teljesült'}
        </span>
      </div>

      {order.active && (onEdit || onCancel) && (
        <div className="flex flex-wrap justify-end gap-2">
          <Button variant="outline" size="sm" leftIcon={<Pencil />} onClick={onEdit}>
            Módosítás
          </Button>
          <Button variant="ghost" size="sm" leftIcon={<Ban />} className="text-danger hover:bg-danger/10 hover:text-danger" onClick={onCancel}>
            Megszüntetés
          </Button>
        </div>
      )}
    </div>
  )
}

// Rendszeres átutalások: az aktív megbízások a következő teljesítéssel, a havi teher összesítve, módosítás és
// megszüntetés; a megszüntetettek összecsukva a lista végén
export default function StandingOrdersPage() {
  const ordersQuery = useStandingOrders()
  const { data: accounts = [] } = useAccounts()
  const { data: beneficiaries = [] } = useBeneficiaries()
  const cancelOrder = useCancelStandingOrder()
  const [editor, setEditor] = useState<{ open: boolean; order?: StandingOrderResponse; key: number }>({ open: false, key: 0 })
  const [cancelling, setCancelling] = useState<StandingOrderResponse>()
  const [now] = useState(() => new Date())

  const orders = ordersQuery.data ?? []
  const activeOrders = orders.filter((order) => order.active)
  const inactiveOrders = orders.filter((order) => !order.active)
  const hasActiveAccount = accounts.some((account) => account.status === 'ACTIVE')

  function openEditor(order?: StandingOrderResponse) {
    setEditor((current) => ({ open: true, order, key: current.key + 1 }))
  }

  function confirmCancel() {
    if (!cancelling) {
      return
    }
    cancelOrder.mutate(cancelling.id, {
      onSuccess: () => {
        toast.success('Megszüntetted a rendszeres átutalást', { description: cancelling.partnerName })
        setCancelling(undefined)
      },
    })
  }

  return (
    <>
      <PageHeader
        title="Rendszeres átutalások"
        description="Ismétlődő utalások (pl. albérlet, rezsi) – a FineX magától elküldi őket."
        actions={
          <Button variant="glass" leftIcon={<Plus />} disabled={!hasActiveAccount} onClick={() => openEditor()}>
            Új megbízás
          </Button>
        }
      />

      {ordersQuery.isPending ? (
        <StandingOrdersSkeleton />
      ) : ordersQuery.isError ? (
        <Card>
          <QueryError message={getErrorMessage(ordersQuery.error)} retrying={ordersQuery.isFetching} onRetry={() => void ordersQuery.refetch()} />
        </Card>
      ) : activeOrders.length === 0 && inactiveOrders.length === 0 ? (
        <Card>
          <EmptyState
            icon={<CalendarClock />}
            title="Még nincs rendszeres átutalásod"
            description="Állítsd be egyszer az albérletet vagy a rezsit, és minden hónapban magától elmegy."
            action={
              <Button leftIcon={<Plus />} disabled={!hasActiveAccount} onClick={() => openEditor()}>
                Megbízás beállítása
              </Button>
            }
          />
        </Card>
      ) : (
        <>
          {activeOrders.length > 0 && (
            <Card className="p-6">
              <CardHeader title="Havonta nagyjából" description={`${activeOrders.length} aktív megbízás (a heti utalások havi összegre átszámítva)`} />
              <dl className="mt-4 grid grid-cols-[repeat(auto-fit,minmax(min(14rem,100%),1fr))] gap-3">
                {monthlyTotals(activeOrders).map(([currency, total]) => (
                  <div key={currency} className="rounded-2xl bg-muted/70 p-4">
                    <dt className="text-sm font-medium text-muted-foreground">{currencyName(currency)}</dt>
                    <dd className="mt-1 text-2xl font-extrabold tracking-tight text-foreground">{formatMoney(total, currency)}</dd>
                  </div>
                ))}
              </dl>
            </Card>
          )}

          <motion.ul variants={staggerContainer} initial="hidden" animate="show" className="mt-6 space-y-6">
            {activeOrders.map((order) => (
              <motion.li key={order.id} variants={staggerItem} className={cn(cardSurface, 'spotlight')}>
                <OrderCard
                  order={order}
                  account={accounts.find((account) => account.id === order.accountId)}
                  now={now}
                  onEdit={() => openEditor(order)}
                  onCancel={() => setCancelling(order)}
                />
              </motion.li>
            ))}
          </motion.ul>

          {inactiveOrders.length > 0 && (
            <details className="group mt-8">
              <summary className="flex w-fit cursor-pointer list-none items-center gap-2 rounded-xl px-1 py-1 font-semibold text-muted-foreground transition hover:text-foreground focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none [&::-webkit-details-marker]:hidden">
                <Archive aria-hidden className="size-4" />
                Megszüntetett megbízások ({inactiveOrders.length})
                <ChevronDown aria-hidden className="size-4 transition-transform group-open:rotate-180" />
              </summary>
              <ul className="mt-4 space-y-5">
                {inactiveOrders.map((order) => (
                  <li key={order.id} className={cardSurface}>
                    <OrderCard order={order} account={accounts.find((account) => account.id === order.accountId)} now={now} />
                  </li>
                ))}
              </ul>
            </details>
          )}
        </>
      )}

      <StandingOrderDialog
        key={editor.key}
        open={editor.open}
        order={editor.order}
        accounts={accounts}
        beneficiaries={beneficiaries}
        onClose={() => setEditor((current) => ({ ...current, open: false }))}
      />

      <ConfirmDialog
        open={cancelling !== undefined}
        onClose={() => {
          setCancelling(undefined)
          cancelOrder.reset()
        }}
        onConfirm={confirmCancel}
        loading={cancelOrder.isPending}
        title="Megszünteted a megbízást?"
        description={cancelling ? `A(z) ${cancelling.partnerName} részére szóló rendszeres átutalás többé nem teljesül. A korábbi utalások megmaradnak.` : undefined}
        confirmLabel="Megszüntetés"
      >
        {cancelOrder.isError && <p className="text-sm text-danger">{getErrorMessage(cancelOrder.error)}</p>}
      </ConfirmDialog>
    </>
  )
}
