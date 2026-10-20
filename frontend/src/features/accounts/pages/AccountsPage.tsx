import { Archive, ChevronDown, Plus } from 'lucide-react'
import { motion } from 'motion/react'
import { useState } from 'react'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse } from '../../../api/types'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { useBankSettings } from '../../../hooks/useBankSettings'
import { currencyName, formatMoney } from '../../../lib/format'
import { staggerContainer, staggerItem } from '../../../lib/motion'
import AccountCard from '../components/AccountCard'
import { AccountsSkeleton } from '../components/AccountSkeletons'
import NewAccountTile from '../components/NewAccountTile'
import OpenAccountDialog from '../components/OpenAccountDialog'
import { useAccounts } from '../queries'

// A nyitott számlák egyenlege pénznemenként összesítve (a különböző pénznemek nem adhatók össze)
function totalsByCurrency(accounts: AccountResponse[]) {
  const totals = new Map<string, { total: number; count: number }>()
  for (const account of accounts) {
    const current = totals.get(account.currency) ?? { total: 0, count: 0 }
    totals.set(account.currency, { total: current.total + account.balance, count: current.count + 1 })
  }
  return [...totals.entries()].map(([currency, value]) => ({ currency, ...value }))
}

export default function AccountsPage() {
  const accountsQuery = useAccounts()
  const [opening, setOpening] = useState(false)

  const accounts = accountsQuery.data ?? []
  const openAccounts = accounts.filter((account) => account.status !== 'CLOSED')
  const closedAccounts = accounts.filter((account) => account.status === 'CLOSED')
  // A nyitható számlák száma a backend beállítása (GET /settings)
  const { data: settings } = useBankSettings()
  const limitReached = settings !== undefined && openAccounts.length >= settings.maxAccountsPerUser

  return (
    <>
      <PageHeader
        title="Számlák"
        description="Folyószámláid egy helyen: egyenleg, számlaszám és kivonat."
        actions={
          <Button variant="glass" leftIcon={<Plus />} disabled={accountsQuery.isPending || limitReached} onClick={() => setOpening(true)}>
            Új számla
          </Button>
        }
      />

      {accountsQuery.isPending ? (
        <AccountsSkeleton />
      ) : accountsQuery.isError ? (
        <Card>
          <QueryError message={getErrorMessage(accountsQuery.error)} retrying={accountsQuery.isFetching} onRetry={() => void accountsQuery.refetch()} />
        </Card>
      ) : (
        <>
          <motion.div variants={staggerContainer} initial="hidden" animate="show" className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
            {openAccounts.map((account) => (
              <motion.div key={account.id} variants={staggerItem}>
                <AccountCard account={account} className="h-full" />
              </motion.div>
            ))}
            <motion.div variants={staggerItem}>
              <NewAccountTile className="size-full min-h-48" disabled={limitReached} onClick={() => setOpening(true)} />
            </motion.div>
          </motion.div>

          <Card className="mt-6 p-6">
            <CardHeader title="Összesítés" description="A nyitott számlák egyenlege pénznemenként." />
            {/* A csempék mindig kitöltik a sort (két pénznemnél fele-fele), nem marad üres oszlop */}
            <dl className="mt-5 grid grid-cols-[repeat(auto-fit,minmax(min(14rem,100%),1fr))] gap-3">
              {totalsByCurrency(openAccounts).map((item) => (
                <div key={item.currency} className="rounded-2xl bg-muted/70 p-4">
                  <dt className="text-sm font-medium text-muted-foreground">
                    {currencyName(item.currency)} · {item.count} számla
                  </dt>
                  <dd className="mt-1 text-2xl font-extrabold tracking-tight text-foreground">{formatMoney(item.total, item.currency)}</dd>
                </div>
              ))}
            </dl>
          </Card>

          {closedAccounts.length > 0 && (
            <details className="group mt-8">
              <summary className="flex w-fit cursor-pointer list-none items-center gap-2 rounded-xl px-1 py-1 font-semibold text-muted-foreground transition hover:text-foreground focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none [&::-webkit-details-marker]:hidden">
                <Archive aria-hidden className="size-4" />
                Lezárt számlák ({closedAccounts.length})
                <ChevronDown aria-hidden className="size-4 transition-transform group-open:rotate-180" />
              </summary>
              <div className="mt-4 grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
                {closedAccounts.map((account) => (
                  <AccountCard key={account.id} account={account} className="h-full" />
                ))}
              </div>
            </details>
          )}
        </>
      )}

      <OpenAccountDialog open={opening} onClose={() => setOpening(false)} openAccountCount={openAccounts.length} />
    </>
  )
}
