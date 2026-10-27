import { ReceiptText, Undo2 } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse, TransactionListItem } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Badge from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import EmptyState from '../../../components/ui/EmptyState'
import Pagination from '../../../components/ui/Pagination'
import QueryError from '../../../components/ui/QueryError'
import TextArea from '../../../components/ui/TextArea'
import { cn } from '../../../lib/cn'
import { formatDateTime, formatShortIban, formatSignedMoney, signedAmount, transactionTypeLabels } from '../../../lib/format'
import { useAdminAccountTransactions, useReverseTransaction } from '../queries'
import { TableRowsSkeleton } from './AdminSkeletons'

// Csak a sztornó gombot ajánlja fel; hogy sztornózható-e, végül a szerver dönti el
function isReversible(item: TransactionListItem): boolean {
  if (item.reversalOfId !== null || item.reversed) {
    return false
  }
  return item.type === 'OUTCOME' || item.type === 'TRANSFER_OUT' || (item.type === 'INCOME' && item.partnerName === 'Készpénzbefizetés')
}

interface AccountTransactionsDialogProps {
  account: AccountResponse | undefined
  onClose: () => void
}

// Sztornó: az eredeti tétel marad, egy ellentétes javító tétel semlegesíti
export default function AccountTransactionsDialog({ account, onClose }: AccountTransactionsDialogProps) {
  const [page, setPage] = useState(0)
  const [reversing, setReversing] = useState<TransactionListItem>()
  const [reason, setReason] = useState('')
  const transactionsQuery = useAdminAccountTransactions(account?.id, page)
  const reverse = useReverseTransaction()

  const items = transactionsQuery.data?.content ?? []

  function close() {
    onClose()
    setPage(0)
    cancelReverse()
  }

  function cancelReverse() {
    setReversing(undefined)
    setReason('')
    reverse.reset()
  }

  function confirmReverse() {
    if (!reversing || !reason.trim()) {
      return
    }
    reverse.mutate(
      { id: reversing.id, reason: reason.trim() },
      {
        onSuccess: () => {
          toast.success('Sztornóztad a tételt', { description: 'Az érintett ügyfelek értesítést kaptak.' })
          cancelReverse()
        },
      },
    )
  }

  return (
    <Dialog
      icon={<ReceiptText />}
      size="lg"
      open={account !== undefined}
      onClose={close}
      title="Számla tételei"
      description={account ? `${account.name} · ${formatShortIban(account.accountNumber)}` : undefined}
    >
      <div className={cn('transition-opacity duration-300', transactionsQuery.isPlaceholderData && 'opacity-50')}>
        {transactionsQuery.isPending ? (
          <TableRowsSkeleton />
        ) : transactionsQuery.isError ? (
          <QueryError message={getErrorMessage(transactionsQuery.error)} retrying={transactionsQuery.isFetching} onRetry={() => void transactionsQuery.refetch()} />
        ) : items.length === 0 ? (
          <EmptyState icon={<ReceiptText />} title="Nincs tétel" description="Ezen a számlán még nem volt mozgás." />
        ) : (
          <ul className="divide-y divide-border">
            {items.map((item) => (
              <li key={item.id} className="flex flex-wrap items-center gap-x-4 gap-y-2 py-3">
                <span className="min-w-0 flex-1">
                  <span className="block truncate font-semibold text-foreground">{item.partnerName ?? transactionTypeLabels[item.type]}</span>
                  <span className="block text-xs text-muted-foreground tabular-nums">
                    #{item.id} · {formatDateTime(item.createdAt)} · {transactionTypeLabels[item.type]}
                  </span>
                </span>
                {item.reversalOfId !== null && <Badge tone="brand">Sztornó (#{item.reversalOfId})</Badge>}
                {item.reversed && <Badge tone="neutral">Sztornózva</Badge>}
                <span className={cn('font-bold whitespace-nowrap tabular-nums', signedAmount(item.type, item.amount) > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-foreground')}>
                  {formatSignedMoney(signedAmount(item.type, item.amount), item.currency)}
                </span>
                {isReversible(item) && (
                  <Button variant="outline" size="sm" leftIcon={<Undo2 />} onClick={() => setReversing(item)} disabled={reverse.isPending}>
                    Sztornó
                  </Button>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>

      {transactionsQuery.data && transactionsQuery.data.page.totalPages > 1 && (
        <div className="mt-4">
          <Pagination page={page} totalPages={transactionsQuery.data.page.totalPages} onPageChange={setPage} />
        </div>
      )}

      {reversing && (
        <div className="mt-5 space-y-4 rounded-2xl border border-border bg-muted/40 p-4">
          <p className="text-sm text-foreground">
            <span className="font-semibold">Sztornó:</span> #{reversing.id} · {reversing.partnerName ?? transactionTypeLabels[reversing.type]} ·{' '}
            {formatSignedMoney(signedAmount(reversing.type, reversing.amount), reversing.currency)}
          </p>
          <TextArea
            label="Indoklás"
            placeholder="pl. Kétszer terhelt kártyás fizetés"
            maxLength={200}
            value={reason}
            onChange={(event) => setReason(event.target.value)}
            hint="A javító tétel közleményébe és az audit naplóba kerül."
          />
          {reverse.isError && <Alert tone="danger">{getErrorMessage(reverse.error)}</Alert>}
          <DialogActions>
            <Button variant="ghost" onClick={cancelReverse}>
              Mégse
            </Button>
            <Button variant="danger" leftIcon={<Undo2 />} loading={reverse.isPending} disabled={!reason.trim()} onClick={confirmReverse}>
              Sztornózás
            </Button>
          </DialogActions>
        </div>
      )}
    </Dialog>
  )
}
