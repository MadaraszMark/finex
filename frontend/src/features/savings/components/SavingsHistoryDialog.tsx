import { ArrowDownLeft, ArrowUpRight, ChevronDown, History, ReceiptText, Sparkles } from 'lucide-react'
import { getErrorMessage } from '../../../api/client'
import type { SavingsAccountResponse, SavingsTransactionType } from '../../../api/types'
import Button from '../../../components/ui/Button'
import Dialog from '../../../components/ui/Dialog'
import EmptyState from '../../../components/ui/EmptyState'
import QueryError from '../../../components/ui/QueryError'
import Skeleton from '../../../components/ui/Skeleton'
import { cn } from '../../../lib/cn'
import { formatDateTime, formatMoney, formatSignedMoney } from '../../../lib/format'
import { useSavingsTransactions } from '../queries'

const typeInfo: Record<SavingsTransactionType, { label: string; icon: typeof Sparkles; sign: 1 | -1 }> = {
  DEPOSIT: { label: 'Befizetés', icon: ArrowDownLeft, sign: 1 },
  WITHDRAWAL: { label: 'Kivét', icon: ArrowUpRight, sign: -1 },
  INTEREST: { label: 'Kamatjóváírás', icon: Sparkles, sign: 1 },
}

// A megtakarítás mozgásai (befizetés, kivét, havi kamat) a mozgás utáni egyenleggel, lapozva
export default function SavingsHistoryDialog({ savings, open, onClose }: { savings: SavingsAccountResponse; open: boolean; onClose: () => void }) {
  const historyQuery = useSavingsTransactions(savings.id)
  const items = historyQuery.data?.pages.flatMap((page) => page.content) ?? []

  return (
    <Dialog icon={<History />} open={open} onClose={onClose} title="Mozgások" description={savings.name}>
      {historyQuery.isPending ? (
        <div className="space-y-3">
          {[0, 1, 2, 3].map((index) => (
            <Skeleton key={index} className="h-14" />
          ))}
        </div>
      ) : historyQuery.isError ? (
        <QueryError message={getErrorMessage(historyQuery.error)} retrying={historyQuery.isFetching} onRetry={() => void historyQuery.refetch()} />
      ) : items.length === 0 ? (
        <EmptyState icon={<ReceiptText />} title="Még nincs mozgás" description="A befizetések, kivétek és kamatjóváírások itt jelennek meg." className="py-8" />
      ) : (
        <>
          <ul className="max-h-[60dvh] divide-y divide-border overflow-y-auto pr-1">
            {items.map((item) => {
              const info = typeInfo[item.type]
              return (
                <li key={item.id} className="flex items-center gap-3.5 py-3">
                  <span
                    className={cn(
                      'flex size-11 shrink-0 items-center justify-center rounded-2xl',
                      info.sign > 0 ? 'bg-success/12 text-emerald-700 dark:text-emerald-400' : 'bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300',
                    )}
                  >
                    <info.icon aria-hidden className="size-5" />
                  </span>
                  <span className="min-w-0 flex-1">
                    <span className="block font-bold text-foreground">{info.label}</span>
                    <span className="block text-sm text-muted-foreground">{formatDateTime(item.createdAt)}</span>
                  </span>
                  <span className="shrink-0 text-right">
                    <span className={cn('block font-bold tabular-nums', info.sign > 0 ? 'text-emerald-700 dark:text-emerald-400' : 'text-foreground')}>
                      {formatSignedMoney(info.sign * item.amount, savings.currency)}
                    </span>
                    <span className="block text-sm text-muted-foreground tabular-nums">{formatMoney(item.balanceAfter, savings.currency)}</span>
                  </span>
                </li>
              )
            })}
          </ul>
          {historyQuery.hasNextPage && (
            <div className="mt-4 flex justify-center">
              <Button variant="outline" size="sm" rightIcon={<ChevronDown />} loading={historyQuery.isFetchingNextPage} onClick={() => void historyQuery.fetchNextPage()}>
                További mozgások
              </Button>
            </div>
          )}
        </>
      )}
    </Dialog>
  )
}
