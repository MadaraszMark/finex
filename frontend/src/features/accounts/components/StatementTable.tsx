import { ChevronDown, ReceiptText } from 'lucide-react'
import { useState } from 'react'
import type { StatementItemResponse } from '../../../api/types'
import Button from '../../../components/ui/Button'
import EmptyState from '../../../components/ui/EmptyState'
import { cn } from '../../../lib/cn'
import { formatMoney, formatSignedMoney, formatTime, transactionTypeLabels } from '../../../lib/format'

const PAGE_SIZE = 30
const dateFormatter = new Intl.DateTimeFormat('hu-HU', { year: 'numeric', month: 'short', day: 'numeric' })

interface StatementTableProps {
  items: StatementItemResponse[]
  currency: string
  refreshing?: boolean
}

// A kivonat tételei időrendben, a tétel utáni (futó) egyenleggel, mint egy banki kivonaton.
// Hosszú időszaknál részletekben jelenik meg; mobilon az egyenleg az összeg alá kerül.
export default function StatementTable({ items, currency, refreshing = false }: StatementTableProps) {
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)

  if (items.length === 0) {
    return <EmptyState icon={<ReceiptText />} title="Nincs forgalom" description="Ebben az időszakban nem volt tétel a számlán." className="py-10" />
  }

  const visibleItems = items.slice(0, visibleCount)
  const remaining = items.length - visibleItems.length

  return (
    <div className={cn('transition-opacity duration-300', refreshing && 'opacity-50')}>
      <table className="w-full text-sm">
        <caption className="sr-only">Számlakivonat tételei időrendben, futó egyenleggel</caption>
        <thead>
          <tr className="border-b border-border text-left text-xs font-semibold tracking-wide text-muted-foreground uppercase">
            <th scope="col" className="py-3 pr-3 font-semibold">
              Dátum
            </th>
            <th scope="col" className="py-3 pr-3 font-semibold">
              Megnevezés
            </th>
            <th scope="col" className="py-3 pr-3 text-right font-semibold">
              Összeg
            </th>
            <th scope="col" className="hidden py-3 text-right font-semibold sm:table-cell">
              Egyenleg
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {visibleItems.map((item) => {
            const date = new Date(item.createdAt)
            const incoming = item.signedAmount > 0
            return (
              <tr key={item.transactionId} className="align-top">
                <td className="py-3 pr-3 whitespace-nowrap">
                  <span className="block font-medium text-foreground">{dateFormatter.format(date)}</span>
                  <span className="block text-muted-foreground">{formatTime(item.createdAt)}</span>
                </td>
                <td className="py-3 pr-3">
                  <span className="block font-semibold break-words text-foreground">{item.partnerName ?? transactionTypeLabels[item.type]}</span>
                  <span className="block break-words text-muted-foreground">{item.message ?? transactionTypeLabels[item.type]}</span>
                </td>
                <td className="py-3 pr-3 text-right whitespace-nowrap">
                  <span className={cn('block font-bold tabular-nums', incoming ? 'text-emerald-700 dark:text-emerald-400' : 'text-foreground')}>
                    {formatSignedMoney(item.signedAmount, currency)}
                  </span>
                  <span className="block text-muted-foreground tabular-nums sm:hidden">{formatMoney(item.runningBalance, currency)}</span>
                </td>
                <td className="hidden py-3 text-right whitespace-nowrap text-muted-foreground tabular-nums sm:table-cell">
                  {formatMoney(item.runningBalance, currency)}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>

      {remaining > 0 && (
        <div className="mt-4 flex justify-center">
          <Button variant="outline" size="sm" rightIcon={<ChevronDown />} onClick={() => setVisibleCount((count) => count + PAGE_SIZE)}>
            További tételek ({remaining})
          </Button>
        </div>
      )}
    </div>
  )
}
