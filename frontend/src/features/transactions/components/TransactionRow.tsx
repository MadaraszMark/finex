import type { TransactionListItem } from '../../../api/types'
import { cn } from '../../../lib/cn'
import { formatRelativeTime, formatSignedMoney, formatTime, isIncoming, signedAmount, transactionTypeLabels } from '../../../lib/format'
import { transactionIcon } from '../categoryIcons'

interface TransactionRowProps {
  transaction: TransactionListItem
  now: Date
  // Megadva a sor gomb lesz, amely a tétel adatlapját nyitja meg
  onSelect?: (id: number) => void
  // A napok szerint csoportosított listában elég az időpont (a nap a fejlécben van)
  timeOnly?: boolean
}

// Egy tétel a listákban (a koncepciókép "Legutóbbi tranzakciók" sora): kategóriaikon, partner, kategória,
// előjeles összeg (a jóváírás zöld) és az idő ("12 perce", "Tegnap", "szept. 28.")
export default function TransactionRow({ transaction, now, onSelect, timeOnly = false }: TransactionRowProps) {
  const incoming = isIncoming(transaction.type)
  const category = transaction.categories[0]
  const typeLabel = transactionTypeLabels[transaction.type]

  const content = (
    <>
      <span
        className={cn(
          'flex size-12 shrink-0 items-center justify-center rounded-2xl',
          incoming ? 'bg-success/12 text-emerald-700 dark:text-emerald-400' : 'bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300',
        )}
      >
        {transactionIcon(transaction.type, category?.icon, 'size-5')}
      </span>
      <span className="min-w-0 flex-1">
        <span className="block truncate font-bold text-foreground">{transaction.partnerName ?? typeLabel}</span>
        <span className="block truncate text-sm text-muted-foreground">{category?.name ?? transaction.message ?? typeLabel}</span>
      </span>
      <span className="shrink-0 text-right">
        <span className={cn('block font-bold tabular-nums', incoming ? 'text-emerald-700 dark:text-emerald-400' : 'text-foreground')}>
          {formatSignedMoney(signedAmount(transaction.type, transaction.amount), transaction.currency)}
        </span>
        <span className="block text-sm text-muted-foreground">
          <time dateTime={transaction.createdAt}>{timeOnly ? formatTime(transaction.createdAt) : formatRelativeTime(transaction.createdAt, now)}</time>
        </span>
      </span>
    </>
  )

  if (onSelect) {
    return (
      <button
        type="button"
        onClick={() => onSelect(transaction.id)}
        className="-mx-2 flex w-[calc(100%+1rem)] items-center gap-3.5 rounded-2xl px-2 py-3 text-left transition hover:bg-muted/70 focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
      >
        {content}
      </button>
    )
  }

  return <div className="flex items-center gap-3.5 py-3">{content}</div>
}
