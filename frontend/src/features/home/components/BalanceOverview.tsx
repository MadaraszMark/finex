import { ArrowDownLeft, ArrowUpRight, Wallet } from 'lucide-react'
import { useState, type ReactNode } from 'react'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse } from '../../../api/types'
import AnimatedNumber from '../../../components/ui/AnimatedNumber'
import { Card } from '../../../components/ui/Card'
import QueryError from '../../../components/ui/QueryError'
import SegmentedControl from '../../../components/ui/SegmentedControl'
import Skeleton from '../../../components/ui/Skeleton'
import { cn } from '../../../lib/cn'
import { currencies, currencyName, formatMoney } from '../../../lib/format'
import MonthlyChart from '../../statistics/components/MonthlyChart'
import { useMonthlySummary, useSummary } from '../../statistics/queries'
import SavingsSnapshot from './SavingsSnapshot'

const monthFormatter = new Intl.DateTimeFormat('hu-HU', { month: 'long' })

interface StatCardProps {
  icon: ReactNode
  label: string
  value: number
  currency: string
  meta: string
  className?: string
}

// Egy mutató kártyán: címke ikonnal, az összeg (a kártya szélességéhez igazodó betűmérettel, így keskeny oszlopban
// sem törik szét) és egy magyarázó sor. Pénznemváltáskor az összeg nulláról számol fel, nem a régiből "pörög át".
function StatCard({ icon, label, value, currency, meta, className }: StatCardProps) {
  return (
    <Card className={cn('@container flex flex-col p-5 sm:p-6', className)}>
      <dt className="flex items-center gap-2 text-sm font-semibold text-muted-foreground [&_svg]:size-4 [&_svg]:shrink-0">
        {icon}
        {label}
      </dt>
      <dd className="mt-2">
        <AnimatedNumber
          key={currency}
          value={value}
          format={(amount) => formatMoney(amount, currency)}
          className="block text-[clamp(1.125rem,11cqi,2.25rem)] leading-tight font-extrabold tracking-tight break-words text-foreground"
        />
      </dd>
      <dd className="mt-auto pt-2 text-sm text-muted-foreground">{meta}</dd>
    </Card>
  )
}

// A főoldal áttekintése egy pénznemben, a számlakártyák 3 oszlopos rácsához igazodva: felül három mutató (teljes
// egyenleg, havi bevétel és kiadás), alattuk a havi grafikon (két oszlop széles) és a megtakarítások. A pénznemek
// csak azok, amelyekben a felhasználónak nyitott számlája van; minden adat a backendből jön.
export default function BalanceOverview({ accounts }: { accounts: AccountResponse[] }) {
  const available = currencies.filter((currency) => accounts.some((account) => account.currency === currency))
  const [selected, setSelected] = useState<string>()
  const currency = selected && available.includes(selected as (typeof available)[number]) ? selected : (available[0] ?? 'HUF')
  const [month] = useState(() => monthFormatter.format(new Date()))

  const summaryQuery = useSummary(currency)
  const monthlyQuery = useMonthlySummary(currency)
  const summary = summaryQuery.data

  return (
    <section aria-labelledby="overview-title" className="mt-10">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <h2 id="overview-title" className="text-lg font-bold text-foreground">
          Áttekintés
        </h2>
        {available.length > 1 && (
          <SegmentedControl
            label="Pénznem"
            value={currency}
            onChange={setSelected}
            options={available.map((item) => ({ value: item, label: currencyName(item) }))}
          />
        )}
      </div>

      {summaryQuery.isError ? (
        <Card>
          <QueryError message={getErrorMessage(summaryQuery.error)} retrying={summaryQuery.isFetching} onRetry={() => void summaryQuery.refetch()} />
        </Card>
      ) : (
        <dl className={cn('grid grid-cols-2 gap-4 transition-opacity duration-300 lg:grid-cols-3 lg:gap-6', summaryQuery.isPlaceholderData && 'opacity-50')}>
          {summary ? (
            <>
              <StatCard
                icon={<Wallet className="text-primary" />}
                label="Teljes egyenleg"
                value={summary.totalBalance}
                currency={summary.currency}
                meta={`${summary.accountCount} számla · ${summary.activeCardCount} aktív kártya`}
                className="col-span-2 lg:col-span-1"
              />
              <StatCard
                icon={<ArrowDownLeft className="text-chart-income" />}
                label="Bevétel a hónapban"
                value={summary.monthIncome}
                currency={summary.currency}
                meta={`${month} 1. óta`}
              />
              <StatCard
                icon={<ArrowUpRight className="text-chart-outcome" />}
                label="Kiadás a hónapban"
                value={summary.monthOutcome}
                currency={summary.currency}
                meta={`${month} 1. óta`}
              />
            </>
          ) : (
            [0, 1, 2].map((index) => <Skeleton key={index} className={cn('h-32 rounded-3xl', index === 0 && 'col-span-2 lg:col-span-1')} />)
          )}
        </dl>
      )}

      <div className="mt-4 grid grid-cols-1 gap-4 lg:mt-6 lg:grid-cols-3 lg:gap-6">
        <div className="flex min-w-0 lg:col-span-2 [&>*]:min-w-0 [&>*]:flex-1">
          {monthlyQuery.isError ? (
            <Card>
              <QueryError message={getErrorMessage(monthlyQuery.error)} retrying={monthlyQuery.isFetching} onRetry={() => void monthlyQuery.refetch()} />
            </Card>
          ) : (
            <MonthlyChart data={monthlyQuery.data} currency={currency} refreshing={monthlyQuery.isPlaceholderData} />
          )}
        </div>
        <SavingsSnapshot currency={currency} total={summary?.savingsBalance} />
      </div>
    </section>
  )
}
