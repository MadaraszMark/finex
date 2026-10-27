import { ArrowRight, PiggyBank } from 'lucide-react'
import { Link } from 'react-router'
import { getErrorMessage } from '../../../api/client'
import ButtonLink from '../../../components/ui/ButtonLink'
import { Card, CardHeader } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import QueryError from '../../../components/ui/QueryError'
import Skeleton from '../../../components/ui/Skeleton'
import { formatMoney } from '../../../lib/format'
import { useSavings } from '../../savings/queries'

const percentFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 0 })

// Megtakarítások a kiválasztott pénznemben, legfeljebb négy cél haladásával
export default function SavingsSnapshot({ currency, total }: { currency: string; total: number | undefined }) {
  const savingsQuery = useSavings()
  const goals = (savingsQuery.data ?? []).filter((savings) => savings.currency === currency && savings.status !== 'CLOSED').slice(0, 4)

  return (
    <Card className="flex flex-col p-5 sm:p-6">
      <CardHeader
        title="Megtakarítások"
        action={
          <Link
            to="/savings"
            className="group inline-flex items-center gap-1 rounded-lg text-sm font-semibold text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
          >
            Részletek
            <ArrowRight aria-hidden className="size-4 transition-transform group-hover:translate-x-0.5" />
          </Link>
        }
      />

      {total !== undefined && (
        <p className="mt-3">
          <span className="block text-2xl font-extrabold tracking-tight text-foreground">{formatMoney(total, currency)}</span>
          <span className="block text-sm text-muted-foreground">a céljaidon összesen</span>
        </p>
      )}

      {savingsQuery.isPending ? (
        <div className="mt-5 space-y-5">
          {[0, 1, 2].map((index) => (
            <Skeleton key={index} className="h-12" />
          ))}
        </div>
      ) : savingsQuery.isError ? (
        <QueryError message={getErrorMessage(savingsQuery.error)} retrying={savingsQuery.isFetching} onRetry={() => void savingsQuery.refetch()} />
      ) : goals.length === 0 ? (
        <EmptyState
          icon={<PiggyBank />}
          title="Még nincs megtakarításod"
          description="Adj nevet egy célnak, és havonta kamatot írunk jóvá rá."
          action={
            <ButtonLink to="/savings" variant="outline" size="sm">
              Megtakarítások
            </ButtonLink>
          }
          className="my-auto py-6"
        />
      ) : (
        <ul className="mt-5 space-y-5">
          {goals.map((goal) => {
            const percent = goal.targetAmount !== null && goal.targetAmount > 0 ? Math.min((goal.balance / goal.targetAmount) * 100, 100) : undefined
            return (
              <li key={goal.id}>
                <div className="flex items-baseline justify-between gap-3 text-sm">
                  <span className="truncate font-semibold text-foreground">{goal.name}</span>
                  <span className="shrink-0 font-bold text-foreground tabular-nums">{formatMoney(goal.balance, goal.currency)}</span>
                </div>
                {percent !== undefined && goal.targetAmount !== null ? (
                  <>
                    <div
                      role="progressbar"
                      aria-label={`${goal.name}: a cél teljesítése`}
                      aria-valuemin={0}
                      aria-valuemax={100}
                      aria-valuenow={Math.round(percent)}
                      className="mt-2 h-2 overflow-hidden rounded-full bg-muted"
                    >
                      <div className="h-full rounded-full bg-brand-gradient" style={{ width: `${percent}%` }} />
                    </div>
                    <p className="mt-1.5 text-xs text-muted-foreground">
                      {percentFormatter.format(percent)}% · cél: {formatMoney(goal.targetAmount, goal.currency)}
                    </p>
                  </>
                ) : (
                  <p className="mt-1.5 text-xs text-muted-foreground">Cél nélküli gyűjtés</p>
                )}
              </li>
            )
          })}
        </ul>
      )}
    </Card>
  )
}
