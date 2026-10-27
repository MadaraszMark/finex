import { useState } from 'react'
import { getErrorMessage } from '../../api/client'
import { Card } from '../../components/ui/Card'
import PageHeader from '../../components/ui/PageHeader'
import QueryError from '../../components/ui/QueryError'
import { formatLongDate } from '../../lib/format'
import { useCurrentUser } from '../../session/useCurrentUser'
import OpenAccountDialog from '../accounts/components/OpenAccountDialog'
import { useAccounts } from '../accounts/queries'
import CardSpotlight from './CardSpotlight'
import AccountsCarousel from './components/AccountsCarousel'
import BalanceOverview from './components/BalanceOverview'
import HomeSkeleton from './components/HomeSkeleton'
import RecentTransactions from './components/RecentTransactions'

// Főoldal: minden sor ugyanarra a 3 oszlopos rácsra illeszkedik, egyforma magas kártyákkal
export default function HomePage() {
  const { data: user } = useCurrentUser()
  const [today] = useState(() => formatLongDate(new Date()))
  const accountsQuery = useAccounts()
  const [opening, setOpening] = useState(false)
  const openAccounts = (accountsQuery.data ?? []).filter((account) => account.status !== 'CLOSED')

  return (
    <>
      <PageHeader
        eyebrow={today}
        title={user ? 'Szia,' : 'Szia!'}
        highlight={user ? `${user.firstName}!` : undefined}
        description="Így állnak ma a pénzügyeid."
      />

      {accountsQuery.isPending ? (
        <HomeSkeleton />
      ) : (
        <>
          {accountsQuery.isError ? (
            <Card>
              <QueryError message={getErrorMessage(accountsQuery.error)} retrying={accountsQuery.isFetching} onRetry={() => void accountsQuery.refetch()} />
            </Card>
          ) : (
            <AccountsCarousel accounts={openAccounts} onOpenAccount={() => setOpening(true)} />
          )}

          {openAccounts.length > 0 && <BalanceOverview accounts={openAccounts} />}

          <div className="mt-4 grid grid-cols-1 gap-4 lg:mt-6 lg:grid-cols-3 lg:gap-6">
            <RecentTransactions className="lg:col-span-2" />
            <CardSpotlight />
          </div>
        </>
      )}

      <OpenAccountDialog open={opening} onClose={() => setOpening(false)} openAccountCount={openAccounts.length} />
    </>
  )
}
