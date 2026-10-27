import { ReceiptText } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router'
import type { TransactionListItem } from '../../../api/types'
import { SkeletonRows } from '../../../components/skeletons/SkeletonKit'
import { Card, CardHeader } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import { cn } from '../../../lib/cn'
import TransactionRow from '../../transactions/components/TransactionRow'

interface TopExpensesProps {
  data: TransactionListItem[] | undefined
  periodLabel: string
  refreshing?: boolean
}

// Az időszak legnagyobb kiadásai; kattintásra a tétel adatlapja nyílik meg
export default function TopExpenses({ data, periodLabel, refreshing = false }: TopExpensesProps) {
  const navigate = useNavigate()
  const [now] = useState(() => new Date())

  return (
    <Card className="flex flex-col p-5 sm:p-6">
      <CardHeader title="Legnagyobb kiadások" description={periodLabel} />
      <div className={cn('mt-2 transition-opacity duration-300', refreshing && 'opacity-50')}>
        {!data ? (
          <SkeletonRows count={6} />
        ) : data.length === 0 ? (
          <EmptyState icon={<ReceiptText />} title="Nincs kiadás" description="Ebben az időszakban nem volt kimenő tétel." className="py-8" />
        ) : (
          <ul className="divide-y divide-border">
            {data.map((transaction) => (
              <li key={transaction.id}>
                <TransactionRow transaction={transaction} now={now} onSelect={(id) => navigate(`/transactions?tx=${id}`)} />
              </li>
            ))}
          </ul>
        )}
      </div>
    </Card>
  )
}
