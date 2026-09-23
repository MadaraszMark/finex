import type { UseQueryResult } from '@tanstack/react-query'
import { ArrowDownLeft, ArrowUpRight } from 'lucide-react'
import { getErrorMessage } from '../api/client'
import type { Page, TransactionListItem } from '../api/types'
import { formatDateTime, formatMoney, isIncoming, transactionTypeLabels } from '../lib/format'

type TransactionsQuery = UseQueryResult<Page<TransactionListItem>>

function TransactionRow({ transaction }: { transaction: TransactionListItem }) {
  const incoming = isIncoming(transaction.type)
  const typeLabel = transactionTypeLabels[transaction.type]

  return (
    <li className="flex items-center gap-3 py-3">
      <span
        className={`flex size-10 shrink-0 items-center justify-center rounded-xl ${incoming ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-500'}`}
      >
        {incoming ? <ArrowDownLeft className="size-5" /> : <ArrowUpRight className="size-5" />}
      </span>
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-slate-800">{transaction.message ?? typeLabel}</p>
        <p className="text-xs text-slate-500">
          {typeLabel} · {formatDateTime(transaction.createdAt)}
        </p>
      </div>
      <p className={`text-sm font-semibold whitespace-nowrap ${incoming ? 'text-emerald-600' : 'text-slate-800'}`}>
        {incoming ? '+' : '−'}
        {formatMoney(transaction.amount, transaction.currency)}
      </p>
    </li>
  )
}

function TransactionListBody({ query }: { query: TransactionsQuery }) {
  if (query.isPending) {
    return (
      <div className="space-y-3 py-3">
        {[1, 2, 3].map((row) => (
          <div key={row} className="h-10 animate-pulse rounded-xl bg-slate-100" />
        ))}
      </div>
    )
  }
  if (query.isError) {
    return <p className="py-3 text-sm text-red-600">{getErrorMessage(query.error)}</p>
  }
  if (query.data.content.length === 0) {
    return <p className="py-3 text-sm text-slate-500">Még nincs tranzakció. Próbáld ki a teszt befizetést!</p>
  }
  return (
    <ul className="divide-y divide-slate-100">
      {query.data.content.map((transaction) => (
        <TransactionRow key={transaction.id} transaction={transaction} />
      ))}
    </ul>
  )
}

export default function TransactionList({ query }: { query: TransactionsQuery }) {
  return (
    <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
      <div className="flex items-baseline justify-between">
        <h2 className="font-semibold text-slate-900">Legutóbbi tranzakciók</h2>
        {query.data && <span className="text-xs text-slate-500">összesen {query.data.totalElements} db</span>}
      </div>
      <TransactionListBody query={query} />
    </section>
  )
}
