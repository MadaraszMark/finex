import { ArrowLeft, Plus, SearchX } from 'lucide-react'
import { useState } from 'react'
import { useParams } from 'react-router'
import { getErrorMessage, isNotFoundError } from '../../../api/client'
import ButtonLink from '../../../components/ui/ButtonLink'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import Skeleton from '../../../components/ui/Skeleton'
import { addDays } from '../../../lib/dates'
import { accountTypeLabels, currencyName, formatIsoDate, parseIsoDate } from '../../../lib/format'
import { useCards } from '../../cards/queries'
import AccountOverview from '../components/AccountOverview'
import { AccountDetailSkeleton } from '../components/AccountSkeletons'
import BalanceChart, { type BalancePoint } from '../components/BalanceChart'
import CloseAccountCard from '../components/CloseAccountCard'
import DepositDialog from '../components/DepositDialog'
import PeriodFilter from '../components/PeriodFilter'
import PeriodSummary from '../components/PeriodSummary'
import StatementTable from '../components/StatementTable'
import { usePeriod } from '../period'
import { useAccount, useAccounts, useBalanceHistory, useStatement } from '../queries'

function AccountNotFound() {
  return (
    <>
      <PageHeader title="A számla nem található" description="Lehet, hogy elgépelted a címet, vagy ez a számla nem a tiéd." />
      <Card>
        <EmptyState
          icon={<SearchX />}
          title="Nincs ilyen számlád"
          action={
            <ButtonLink to="/accounts" leftIcon={<ArrowLeft />}>
              Vissza a számlákhoz
            </ButtonLink>
          }
        />
      </Card>
    </>
  )
}

// Számlaadatlap (/accounts/:accountId): egyenleg, számlaszám, kártya; alatta egy időszakszűrő, amely az
// egyenleggrafikont, az időszak számait és a kivonatot egyszerre szűri; legalul a számla lezárása
export default function AccountDetailPage() {
  const accountId = Number(useParams().accountId)
  if (!Number.isSafeInteger(accountId) || accountId <= 0) {
    return <AccountNotFound />
  }
  return <AccountDetail key={accountId} accountId={accountId} />
}

function AccountDetail({ accountId }: { accountId: number }) {
  const accountQuery = useAccount(accountId)
  const accountsQuery = useAccounts()
  const cardsQuery = useCards()
  const { period, today, selectPreset, selectRange } = usePeriod()
  const statementQuery = useStatement(accountId, period.from, period.to)
  const historyQuery = useBalanceHistory(accountId, period.from, period.to)
  const [depositOpen, setDepositOpen] = useState(false)
  const [now] = useState(() => new Date())

  if (isNotFoundError(accountQuery.error)) {
    return <AccountNotFound />
  }

  const account = accountQuery.data
  if (!account) {
    return (
      <>
        <PageHeader title="Számlaadatlap" />
        {accountQuery.isError ? (
          <Card>
            <QueryError message={getErrorMessage(accountQuery.error)} retrying={accountQuery.isFetching} onRetry={() => void accountQuery.refetch()} />
          </Card>
        ) : (
          <AccountDetailSkeleton />
        )}
      </>
    )
  }

  const card = cardsQuery.data?.find((item) => item.accountId === account.id && item.status !== 'CANCELLED') ?? cardsQuery.data?.find((item) => item.accountId === account.id)
  const openAccountCount = accountsQuery.data?.filter((item) => item.status !== 'CLOSED').length ?? 1

  const statement = statementQuery.data
  // Az időtengely a (megjelenített) kivonat időszaka: a kezdő nap elejétől a záró nap végéig, a mai napnál csak a
  // mostani pillanatig. Időszakváltás közben még az előző kivonat látszik, ezért annak az időszakát követjük.
  const from = parseIsoDate(statement?.from ?? period.from).getTime()
  const to = Math.min(addDays(parseIsoDate(statement?.to ?? period.to), 1).getTime() - 1, now.getTime())
  // A grafikon a kivonat nyitó egyenlegével indul, az egyenlegtörténet pontjain át a záró egyenlegig tart
  const points: BalancePoint[] | undefined =
    statement && historyQuery.data
      ? [
          { time: from, balance: statement.openingBalance },
          ...historyQuery.data
            .map((item) => ({ time: new Date(item.createdAt).getTime(), balance: item.balance }))
            .filter((point) => point.time >= from && point.time <= to),
          { time: to, balance: statement.closingBalance },
        ]
      : undefined
  const periodLabel = `${formatIsoDate(period.from)} – ${formatIsoDate(period.to)}`

  return (
    <>
      <PageHeader
        eyebrow={`${accountTypeLabels[account.accountType]} · ${currencyName(account.currency)}`}
        title={account.name}
        actions={
          account.status === 'ACTIVE' && (
            <Button variant="glass" leftIcon={<Plus />} onClick={() => setDepositOpen(true)}>
              Befizetés
            </Button>
          )
        }
      />

      <AccountOverview account={account} card={card} now={now} />

      <section aria-labelledby="account-activity" className="mt-10">
        <h2 id="account-activity" className="mb-4 text-lg font-bold text-foreground">
          Egyenleg és forgalom
        </h2>
        <PeriodFilter period={period} today={today} onPresetChange={selectPreset} onRangeChange={selectRange} />

        {statementQuery.isError || historyQuery.isError ? (
          <Card className="mt-5">
            <QueryError
              message={getErrorMessage(statementQuery.error ?? historyQuery.error)}
              retrying={statementQuery.isFetching || historyQuery.isFetching}
              onRetry={() => {
                void statementQuery.refetch()
                void historyQuery.refetch()
              }}
            />
          </Card>
        ) : (
          <>
            <div className="mt-5 grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)]">
              <Card className="p-5 sm:p-6">
                <CardHeader title="Egyenleg alakulása" description={periodLabel} />
                <figure className="mt-4">
                  {points ? (
                    <BalanceChart
                      points={points}
                      currency={account.currency}
                      from={from}
                      to={to}
                      refreshing={statementQuery.isPlaceholderData || historyQuery.isPlaceholderData}
                    />
                  ) : (
                    <Skeleton className="h-64" />
                  )}
                  <figcaption className="sr-only">Az egyenleg alakulása a kiválasztott időszakban; a pontos értékek a lenti kivonatban.</figcaption>
                </figure>
              </Card>
              <Card className="p-5 sm:p-6">
                <CardHeader title="Az időszak számai" description={periodLabel} />
                <div className="mt-4">
                  {statement ? (
                    <PeriodSummary statement={statement} refreshing={statementQuery.isPlaceholderData} />
                  ) : (
                    <div className="grid grid-cols-2 gap-3">
                      {[0, 1, 2, 3].map((index) => (
                        <Skeleton key={index} className="h-20" />
                      ))}
                    </div>
                  )}
                </div>
              </Card>
            </div>

            <Card className="mt-6 p-5 sm:p-6">
              <CardHeader title="Számlakivonat" description={`${periodLabel} · a tételek időrendben, futó egyenleggel`} />
              <div className="mt-4">
                {statement ? (
                  <StatementTable
                    key={`${period.from}_${period.to}`}
                    items={statement.items}
                    currency={statement.currency}
                    refreshing={statementQuery.isPlaceholderData}
                  />
                ) : (
                  <div className="space-y-3">
                    {[0, 1, 2, 3].map((index) => (
                      <Skeleton key={index} className="h-12" />
                    ))}
                  </div>
                )}
              </div>
            </Card>
          </>
        )}
      </section>

      {account.status !== 'CLOSED' && (
        <div className="mt-6">
          <CloseAccountCard account={account} openAccountCount={openAccountCount} />
        </div>
      )}

      <DepositDialog account={account} open={depositOpen} onClose={() => setDepositOpen(false)} />
    </>
  )
}
