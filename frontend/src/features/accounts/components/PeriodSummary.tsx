import { ArrowDownLeft, ArrowUpRight, Flag, FlagTriangleRight } from 'lucide-react'
import type { StatementResponse } from '../../../api/types'
import { cn } from '../../../lib/cn'
import { formatMoney, formatSignedMoney } from '../../../lib/format'

// Az időszak számai: nyitó és záró egyenleg, jóváírások és terhelések összege
export default function PeriodSummary({ statement, refreshing = false }: { statement: StatementResponse; refreshing?: boolean }) {
  const tiles = [
    { label: 'Nyitó egyenleg', value: formatMoney(statement.openingBalance, statement.currency), icon: Flag, tone: 'neutral' },
    { label: 'Jóváírások', value: formatSignedMoney(statement.totalIncome, statement.currency), icon: ArrowDownLeft, tone: 'income' },
    { label: 'Terhelések', value: formatSignedMoney(-statement.totalOutcome, statement.currency), icon: ArrowUpRight, tone: 'outcome' },
    { label: 'Záró egyenleg', value: formatMoney(statement.closingBalance, statement.currency), icon: FlagTriangleRight, tone: 'neutral' },
  ] as const

  return (
    <dl className={cn('grid grid-cols-2 gap-3 transition-opacity duration-300', refreshing && 'opacity-50')}>
      {tiles.map((tile) => (
        <div key={tile.label} className="rounded-2xl bg-muted/70 p-4">
          <dt className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
            <tile.icon
              aria-hidden
              className={cn(
                'size-4',
                tile.tone === 'income' && 'text-chart-income',
                tile.tone === 'outcome' && 'text-chart-outcome',
                tile.tone === 'neutral' && 'text-muted-foreground',
              )}
            />
            {tile.label}
          </dt>
          <dd className="mt-1.5 text-lg font-bold tracking-tight break-words text-foreground">{tile.value}</dd>
        </div>
      ))}
    </dl>
  )
}
