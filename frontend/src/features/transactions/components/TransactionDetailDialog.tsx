import { ReceiptText, Repeat, Send, X } from 'lucide-react'
import { useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { TransactionResponse } from '../../../api/types'
import Button from '../../../components/ui/Button'
import Dialog from '../../../components/ui/Dialog'
import QueryError from '../../../components/ui/QueryError'
import Select from '../../../components/ui/Select'
import Skeleton from '../../../components/ui/Skeleton'
import { cn } from '../../../lib/cn'
import {
  formatDateTime,
  formatIban,
  formatMaskedCard,
  formatShortIban,
  formatSignedMoney,
  isIncoming,
  signedAmount,
  transactionTypeLabels,
} from '../../../lib/format'
import { useAccounts } from '../../accounts/queries'
import { useCards } from '../../cards/queries'
import { transactionIcon } from '../categoryIcons'
import { useAssignCategory, useCategories, useRemoveCategory, useTransaction, useTransactionCategories } from '../queries'

// Az utalás varázsló előre kitöltve (címzett, összeg, forrásszámla)
function transferLink(transaction: TransactionResponse): string | undefined {
  const params = new URLSearchParams({ from: String(transaction.accountId) })
  if (transaction.type === 'TRANSFER_OUT' && transaction.toAccount) {
    params.set('to', transaction.toAccount)
    params.set('name', transaction.partnerName ?? '')
    params.set('amount', String(transaction.amount))
    return `/transfer?${params}`
  }
  if (transaction.type === 'TRANSFER_IN' && transaction.fromAccount) {
    params.set('to', transaction.fromAccount)
    params.set('name', transaction.partnerName ?? '')
    return `/transfer?${params}`
  }
  return undefined
}

interface TransactionDetailDialogProps {
  transactionId: number | undefined
  onClose: () => void
}

// Egy tétel adatlapja: összeg, partner, számlák, kártya, közlemény és a kategóriák (hozzáadás, levétel).
// Utalásnál egy gombbal újra lehet utalni ugyanannak a címzettnek, illetve vissza lehet utalni a küldőnek.
export default function TransactionDetailDialog({ transactionId, onClose }: TransactionDetailDialogProps) {
  const navigate = useNavigate()

  // Bezárás közben (animáció alatt) az utoljára megnyitott tétel marad látható
  const [shownId, setShownId] = useState(transactionId)
  if (transactionId !== undefined && transactionId !== shownId) {
    setShownId(transactionId)
  }

  const transactionQuery = useTransaction(shownId)
  const linksQuery = useTransactionCategories(shownId)
  const { data: categories = [] } = useCategories()
  const { data: accounts = [] } = useAccounts()
  const { data: cards = [] } = useCards()
  const assignCategory = useAssignCategory()
  const removeCategory = useRemoveCategory()

  const transaction = transactionQuery.data
  const account = accounts.find((item) => item.id === transaction?.accountId)
  const card = cards.find((item) => item.id === transaction?.cardId)
  const links = linksQuery.data ?? []
  const assignable = categories.filter((category) => !links.some((link) => link.categoryId === category.id))

  function addCategory(categoryId: number) {
    if (!transaction) {
      return
    }
    assignCategory.mutate(
      { transactionId: transaction.id, categoryId },
      { onError: (error) => toast.error(getErrorMessage(error)) },
    )
  }

  function dropCategory(linkId: number) {
    removeCategory.mutate(linkId, { onError: (error) => toast.error(getErrorMessage(error)) })
  }

  return (
    <Dialog icon={<ReceiptText />} open={transactionId !== undefined} onClose={onClose} title="Tétel részletei">
      {transactionQuery.isPending ? (
        <div className="space-y-4">
          <Skeleton className="h-20" />
          <Skeleton className="h-40" />
        </div>
      ) : transactionQuery.isError || !transaction ? (
        <QueryError message={getErrorMessage(transactionQuery.error)} retrying={transactionQuery.isFetching} onRetry={() => void transactionQuery.refetch()} />
      ) : (
        <TransactionDetails
          transaction={transaction}
          accountLabel={account ? `${account.name} · ${formatShortIban(account.accountNumber)}` : undefined}
          cardLabel={card ? formatMaskedCard(card.maskedNumber) : undefined}
        >
          <section aria-labelledby="transaction-categories" className="mt-6">
            <h3 id="transaction-categories" className="text-sm font-semibold text-foreground">
              Kategória
            </h3>
            <ul className="mt-2 flex flex-wrap gap-2">
              {links.map((link) => (
                <li key={link.id}>
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-brand-100 py-1 pr-1 pl-3 text-sm font-semibold text-brand-700 dark:bg-brand-950 dark:text-brand-300">
                    {transactionIcon(transaction.type, link.categoryIcon, 'size-4')}
                    {link.categoryName}
                    <button
                      type="button"
                      aria-label={`${link.categoryName} kategória levétele`}
                      disabled={removeCategory.isPending}
                      onClick={() => dropCategory(link.id)}
                      className="flex size-6 items-center justify-center rounded-full transition hover:bg-brand-200 focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none dark:hover:bg-brand-900"
                    >
                      <X className="size-3.5" />
                    </button>
                  </span>
                </li>
              ))}
              {links.length === 0 && !linksQuery.isPending && <li className="text-sm text-muted-foreground">Nincs kategóriába sorolva.</li>}
            </ul>
            {assignable.length > 0 && (
              <Select
                label="Kategória hozzáadása"
                className="mt-4"
                value=""
                disabled={assignCategory.isPending}
                onChange={(event) => event.target.value && addCategory(Number(event.target.value))}
              >
                <option value="">Válassz kategóriát…</option>
                {assignable.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </Select>
            )}
          </section>

          {transferLink(transaction) && (
            <Button
              variant="secondary"
              fullWidth
              className="mt-6"
              leftIcon={transaction.type === 'TRANSFER_OUT' ? <Repeat /> : <Send />}
              onClick={() => navigate(transferLink(transaction) ?? '/transfer')}
            >
              {transaction.type === 'TRANSFER_OUT' ? 'Újra utalom' : 'Utalás neki'}
            </Button>
          )}
        </TransactionDetails>
      )}
    </Dialog>
  )
}

interface TransactionDetailsProps {
  transaction: TransactionResponse
  accountLabel: string | undefined
  cardLabel: string | undefined
  children: ReactNode
}

function TransactionDetails({ transaction, accountLabel, cardLabel, children }: TransactionDetailsProps) {
  const incoming = isIncoming(transaction.type)
  const counterpartIban = incoming ? transaction.fromAccount : transaction.toAccount
  const rows = [
    { label: incoming ? 'Jóváírt számla' : 'Terhelt számla', value: accountLabel },
    { label: incoming ? 'Küldő számlaszáma' : 'Címzett számlaszáma', value: counterpartIban ? formatIban(counterpartIban) : undefined, mono: true },
    { label: 'Bankkártya', value: cardLabel, mono: true },
    { label: 'Közlemény', value: transaction.message ?? undefined },
    { label: 'Időpont', value: formatDateTime(transaction.createdAt) },
    { label: 'Azonosító', value: `#${transaction.id}`, mono: true },
  ].filter((row) => row.value)

  return (
    <div>
      <div className="flex items-center gap-4">
        <span
          className={cn(
            'flex size-14 shrink-0 items-center justify-center rounded-2xl',
            incoming ? 'bg-success/12 text-emerald-700 dark:text-emerald-400' : 'bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300',
          )}
        >
          {transactionIcon(transaction.type, transaction.categories[0]?.icon, 'size-6')}
        </span>
        <div className="min-w-0">
          <p className="truncate text-lg font-bold text-foreground">{transaction.partnerName ?? transactionTypeLabels[transaction.type]}</p>
          <p className="text-sm text-muted-foreground">{transactionTypeLabels[transaction.type]}</p>
        </div>
      </div>

      <p className={cn('mt-5 text-4xl font-extrabold tracking-tight', incoming ? 'text-emerald-700 dark:text-emerald-400' : 'text-foreground')}>
        {formatSignedMoney(signedAmount(transaction.type, transaction.amount), transaction.currency)}
      </p>

      <dl className="mt-6 divide-y divide-border rounded-2xl border border-border">
        {rows.map((row) => (
          <div key={row.label} className="flex flex-col gap-0.5 px-4 py-3 sm:flex-row sm:items-baseline sm:justify-between sm:gap-4">
            <dt className="text-sm text-muted-foreground">{row.label}</dt>
            <dd className={cn('text-sm font-semibold break-all text-foreground sm:text-right', row.mono && 'font-mono tracking-wide')}>{row.value}</dd>
          </div>
        ))}
      </dl>

      {children}
    </div>
  )
}
