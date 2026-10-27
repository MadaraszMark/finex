import { CircleCheck, RefreshCw, TriangleAlert } from 'lucide-react'
import { getErrorMessage } from '../../../api/client'
import { SkeletonRows } from '../../../components/skeletons/SkeletonKit'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import QueryError from '../../../components/ui/QueryError'
import { formatDateTime, formatMoney, formatShortIban } from '../../../lib/format'
import { useReconciliation } from '../queries'

// Főkönyvi egyeztetés: a tárolt egyenlegek egyeznek-e a könyvelt tételekből számolttal
export default function ReconciliationCard() {
  const reconciliationQuery = useReconciliation()
  const result = reconciliationQuery.data

  return (
    <Card className="flex flex-col p-6 lg:col-span-2">
      <CardHeader
        title="Főkönyvi egyeztetés"
        description="A tárolt egyenlegek összevetése a könyvelt tételekkel."
        action={
          <Button variant="ghost" size="sm" leftIcon={<RefreshCw />} loading={reconciliationQuery.isFetching} onClick={() => void reconciliationQuery.refetch()}>
            Újraellenőrzés
          </Button>
        }
      />

      <div className="mt-5">
        {reconciliationQuery.isPending ? (
          <SkeletonRows count={2} />
        ) : reconciliationQuery.isError ? (
          <QueryError message={getErrorMessage(reconciliationQuery.error)} retrying={reconciliationQuery.isFetching} onRetry={() => void reconciliationQuery.refetch()} />
        ) : result?.balanced ? (
          <p className="flex items-start gap-3 rounded-2xl bg-success/10 p-4 text-sm text-foreground">
            <CircleCheck aria-hidden className="mt-0.5 size-5 shrink-0 text-success" />
            <span>
              <span className="block font-semibold">Minden egyenleg egyezik.</span>
              {result.accountCount} számla és {result.savingsCount} megtakarítás egyenlege megegyezik a könyvelt tételekkel ·{' '}
              ellenőrizve: {formatDateTime(result.checkedAt)}
            </span>
          </p>
        ) : (
          result && (
            <div className="space-y-3">
              <p className="flex items-start gap-3 rounded-2xl bg-danger/10 p-4 text-sm text-foreground">
                <TriangleAlert aria-hidden className="mt-0.5 size-5 shrink-0 text-danger" />
                <span>
                  <span className="block font-semibold">{result.mismatches.length} eltérés a főkönyvben.</span>
                  Ezeknél a tárolt egyenleg nem egyezik a könyvelt tételek összegével: vizsgáld ki, mi módosította.
                </span>
              </p>
              <ul className="divide-y divide-border">
                {result.mismatches.map((item) => (
                  <li key={`${item.kind}-${item.id}`} className="flex flex-wrap items-center justify-between gap-2 py-3 text-sm">
                    <span className="font-semibold text-foreground">
                      {item.kind === 'ACCOUNT' ? `Számla ${formatShortIban(item.label)}` : `Megtakarítás: ${item.label}`}
                    </span>
                    <span className="text-muted-foreground tabular-nums">
                      tárolt {formatMoney(item.storedBalance, item.currency)} · könyvelt {formatMoney(item.ledgerBalance, item.currency)} ·{' '}
                      <span className="font-semibold text-danger">eltérés {formatMoney(item.difference, item.currency)}</span>
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          )
        )}
      </div>
    </Card>
  )
}
