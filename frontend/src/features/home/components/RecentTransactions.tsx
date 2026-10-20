import { ArrowRight, ReceiptText } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router'
import { getErrorMessage } from '../../../api/client'
import { SkeletonRows } from '../../../components/skeletons/SkeletonKit'
import { Card, CardHeader } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import QueryError from '../../../components/ui/QueryError'
import { cn } from '../../../lib/cn'
import TransactionRow from '../../transactions/components/TransactionRow'
import { useRecentTransactions } from '../../transactions/queries'

// A koncepciókép "Legutóbbi tranzakciók" listája az összes számláról, a "Részletek" a Tranzakciók oldalra visz
export default function RecentTransactions({ className }: { className?: string }) {
  const transactionsQuery = useRecentTransactions(6)
  const [now] = useState(() => new Date())

  return (
    <Card className={cn('p-5 sm:p-6', className)}>
      <CardHeader
        title="Legutóbbi tranzakciók"
        action={
          <Link
            to="/transactions"
            className="group inline-flex items-center gap-1 rounded-lg text-sm font-semibold text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
          >
            Részletek
            <ArrowRight aria-hidden className="size-4 transition-transform group-hover:translate-x-0.5" />
          </Link>
        }
      />

      {transactionsQuery.isPending ? (
        <SkeletonRows count={6} className="mt-2" />
      ) : transactionsQuery.isError ? (
        <QueryError message={getErrorMessage(transactionsQuery.error)} retrying={transactionsQuery.isFetching} onRetry={() => void transactionsQuery.refetch()} />
      ) : transactionsQuery.data.length === 0 ? (
        <EmptyState icon={<ReceiptText />} title="Még nincs tranzakciód" description="A befizetések, kártyás fizetések és utalások itt jelennek meg." className="py-8" />
      ) : (
        <ul className="mt-2 divide-y divide-border">
          {transactionsQuery.data.map((transaction) => (
            <li key={transaction.id}>
              <TransactionRow transaction={transaction} now={now} />
            </li>
          ))}
        </ul>
      )}
    </Card>
  )
}
