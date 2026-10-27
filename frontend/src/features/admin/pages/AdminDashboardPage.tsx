import { ArrowLeftRight, Banknote, CalendarClock, Landmark, MessagesSquare, Percent, ShieldAlert, TrendingUp, UserX, UsersRound } from 'lucide-react'
import { motion } from 'motion/react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import { SkeletonStatTile } from '../../../components/skeletons/SkeletonKit'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import { cardSurface } from '../../../components/ui/cardStyles'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { cn } from '../../../lib/cn'
import { formatMoney } from '../../../lib/format'
import { staggerContainer, staggerItem } from '../../../lib/motion'
import OpenTicketsCard from '../components/OpenTicketsCard'
import ReconciliationCard from '../components/ReconciliationCard'
import { useAdminDashboard, useRunJob } from '../queries'

const numberFormatter = new Intl.NumberFormat('hu-HU')

const jobs = [
  { job: 'savings-interest', title: 'Havi kamatjóváírás', description: 'Hónaponként egyszer, megtakarításonként', icon: Percent },
  { job: 'standing-orders', title: 'Rendszeres átutalások', description: 'A mára esedékes megbízások teljesítése', icon: CalendarClock },
] as const

// Admin áttekintés: a bank számai, ütemezett feladatok kézi indítása, válaszra váró ticketek
export default function AdminDashboardPage() {
  const dashboardQuery = useAdminDashboard()
  const runJob = useRunJob()
  const dashboard = dashboardQuery.data

  function run(job: (typeof jobs)[number]['job']) {
    runJob.mutate(job, {
      onSuccess: (result) =>
        toast.success(job === 'savings-interest' ? 'Lefutott a kamatjóváírás' : 'Lefutottak a rendszeres átutalások', {
          description: `${result.processed} feldolgozva, ${result.skipped} kihagyva`,
        }),
      onError: (error) => toast.error(getErrorMessage(error)),
    })
  }

  const tiles = dashboard
    ? [
        { label: 'Felhasználók', value: numberFormatter.format(dashboard.userCount), icon: UsersRound },
        { label: 'Letiltott felhasználók', value: numberFormatter.format(dashboard.blockedUserCount), icon: UserX },
        { label: 'Számlák', value: numberFormatter.format(dashboard.accountCount), icon: Landmark },
        { label: 'Betétállomány (Ft)', value: formatMoney(dashboard.totalDepositsHuf, 'HUF'), icon: Banknote },
        { label: 'Mai tranzakciók', value: numberFormatter.format(dashboard.transactionsToday), icon: ArrowLeftRight },
        { label: 'Mai forgalom (Ft)', value: formatMoney(dashboard.transactionVolumeTodayHuf, 'HUF'), icon: TrendingUp },
        { label: 'Nyitott ticketek', value: numberFormatter.format(dashboard.openTicketCount), icon: MessagesSquare, alert: dashboard.openTicketCount > 0 },
        { label: 'Sikertelen belépés (24 óra)', value: numberFormatter.format(dashboard.failedLoginsLast24h), icon: ShieldAlert, alert: dashboard.failedLoginsLast24h > 0 },
      ]
    : undefined

  return (
    <>
      <PageHeader eyebrow="Adminisztráció" title="Admin áttekintés" description="A bank működése egy pillantással." />

      {dashboardQuery.isError ? (
        <Card>
          <QueryError message={getErrorMessage(dashboardQuery.error)} retrying={dashboardQuery.isFetching} onRetry={() => void dashboardQuery.refetch()} />
        </Card>
      ) : (
        <motion.dl variants={staggerContainer} initial="hidden" animate="show" className="grid grid-cols-2 gap-3 lg:grid-cols-4 lg:gap-6">
          {tiles
            ? tiles.map((tile) => (
                <motion.div key={tile.label} variants={staggerItem} className={cn(cardSurface, '@container flex flex-col p-4 sm:p-5')}>
                  <dt className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
                    <tile.icon aria-hidden className={cn('size-4 shrink-0', tile.alert ? 'text-warning' : 'text-primary')} />
                    {tile.label}
                  </dt>
                  <dd className="mt-auto pt-1.5 text-[clamp(1.125rem,12cqi,1.5rem)] font-extrabold tracking-tight break-words text-foreground">{tile.value}</dd>
                </motion.div>
              ))
            : Array.from({ length: 8 }, (_, index) => <SkeletonStatTile key={index} />)}
        </motion.dl>
      )}

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card className="flex flex-col p-6">
          <CardHeader title="Ütemezett feladatok" description="Óránként maguktól is lefutnak; itt azonnal elindíthatók." />
          <div className="mt-5 space-y-3">
            {jobs.map((item) => (
              <div key={item.job} className="flex items-center gap-3 rounded-2xl bg-muted/70 p-4">
                <item.icon aria-hidden className="size-5 shrink-0 text-primary" />
                <div className="min-w-0 flex-1">
                  <p className="font-semibold text-foreground">{item.title}</p>
                  <p className="text-sm text-muted-foreground">{item.description}</p>
                </div>
                <Button size="sm" variant="outline" loading={runJob.isPending && runJob.variables === item.job} onClick={() => run(item.job)}>
                  Futtatás
                </Button>
              </div>
            ))}
          </div>
        </Card>

        <OpenTicketsCard />

        <ReconciliationCard />
      </div>
    </>
  )
}
