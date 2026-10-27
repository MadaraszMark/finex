import { ChevronDown, SearchX, Send } from 'lucide-react'
import { useState } from 'react'
import { getErrorMessage } from '../../../api/client'
import type { TransactionListItem } from '../../../api/types'
import Button from '../../../components/ui/Button'
import ButtonLink from '../../../components/ui/ButtonLink'
import { Card } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { cn } from '../../../lib/cn'
import { formatDayHeading } from '../../../lib/format'
import { useAccounts } from '../../accounts/queries'
import TransactionDetailDialog from '../components/TransactionDetailDialog'
import TransactionFilters from '../components/TransactionFilters'
import TransactionRow from '../components/TransactionRow'
import { TransactionListSkeleton } from '../components/TransactionsSkeleton'
import { useTransactionFilter } from '../filters'
import { useCategories, useTransactionSearch } from '../queries'

interface DayGroup {
  key: string
  heading: string
  items: TransactionListItem[]
}

// A tételek napok szerint csoportosítva (a lista már időrendben, a legújabb elöl érkezik)
function groupByDay(items: TransactionListItem[], now: Date): DayGroup[] {
  const groups: DayGroup[] = []
  for (const item of items) {
    const date = new Date(item.createdAt)
    const key = date.toDateString()
    let group = groups.at(-1)
    if (!group || group.key !== key) {
      group = { key, heading: formatDayHeading(date, now), items: [] }
      groups.push(group)
    }
    group.items.push(item)
  }
  return groups
}

// Tranzakciók: szűrők a címsorban, a találatok napok szerint, lapozva
export default function TransactionsPage() {
  const { filter, activeCount, update, clear, selectedId, select } = useTransactionFilter()
  const searchQuery = useTransactionSearch(filter)
  const { data: accounts = [] } = useAccounts()
  const { data: categories = [] } = useCategories()
  const [now] = useState(() => new Date())

  const items = searchQuery.data?.pages.flatMap((page) => page.content) ?? []
  const total = searchQuery.data?.pages[0]?.page.totalElements ?? 0

  return (
    <>
      <PageHeader
        title="Tranzakciók"
        description="Minden pénzmozgásod kereshetően."
        actions={
          <ButtonLink to="/transfer" variant="glass" leftIcon={<Send />}>
            Új utalás
          </ButtonLink>
        }
      />

      <Card className="p-5 sm:p-6">
        <TransactionFilters filter={filter} activeCount={activeCount} accounts={accounts} categories={categories} onChange={update} onClear={clear} />
      </Card>

      <Card className="mt-6 p-5 sm:p-6">
        <div className="flex items-center justify-between gap-3">
          <h2 className="text-lg font-bold text-foreground" aria-live="polite">
            {searchQuery.isPending ? 'Tételek' : `${total} tétel`}
          </h2>
          {searchQuery.isFetching && !searchQuery.isFetchingNextPage && !searchQuery.isPending && (
            <span className="text-sm font-medium text-muted-foreground motion-safe:animate-pulse">Keresés…</span>
          )}
        </div>

        {searchQuery.isPending ? (
          <TransactionListSkeleton rows={8} />
        ) : searchQuery.isError ? (
          <QueryError message={getErrorMessage(searchQuery.error)} retrying={searchQuery.isFetching} onRetry={() => void searchQuery.refetch()} />
        ) : items.length === 0 ? (
          <EmptyState
            icon={<SearchX />}
            title="Nincs ilyen tétel"
            description={activeCount > 0 ? 'Próbálj más keresőszót vagy tágabb időszakot.' : 'Még nincs egyetlen tranzakciód sem.'}
            action={
              activeCount > 0 ? (
                <Button variant="outline" onClick={clear}>
                  Szűrők törlése
                </Button>
              ) : undefined
            }
          />
        ) : (
          <div className={cn('mt-2 transition-opacity duration-300', searchQuery.isPlaceholderData && 'opacity-50')}>
            {groupByDay(items, now).map((group) => (
              <section key={group.key} aria-label={group.heading} className="mt-4 first:mt-2">
                <h3 className="text-xs font-bold tracking-wider text-muted-foreground uppercase first-letter:uppercase">{group.heading}</h3>
                <ul className="mt-1 divide-y divide-border">
                  {group.items.map((transaction) => (
                    <li key={transaction.id}>
                      <TransactionRow transaction={transaction} now={now} onSelect={select} timeOnly />
                    </li>
                  ))}
                </ul>
              </section>
            ))}

            {searchQuery.hasNextPage && (
              <div className="mt-6 flex justify-center">
                <Button variant="outline" rightIcon={<ChevronDown />} loading={searchQuery.isFetchingNextPage} onClick={() => void searchQuery.fetchNextPage()}>
                  További tételek
                </Button>
              </div>
            )}
          </div>
        )}
      </Card>

      <TransactionDetailDialog transactionId={selectedId} onClose={() => select(undefined)} />
    </>
  )
}
