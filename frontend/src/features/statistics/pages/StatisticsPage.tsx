import { ArrowDownLeft, ArrowUpRight, PiggyBank, Scale } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { getErrorMessage } from '../../../api/client'
import type { MonthlySummaryResponse } from '../../../api/types'
import { SkeletonStatTile } from '../../../components/skeletons/SkeletonKit'
import { Card } from '../../../components/ui/Card'
import ChipGroup from '../../../components/ui/ChipGroup'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import SegmentedControl from '../../../components/ui/SegmentedControl'
import { cn } from '../../../lib/cn'
import { addMonths, toIsoDate } from '../../../lib/dates'
import { currencies, currencyName, formatIsoDate, formatMoney, formatSignedMoney } from '../../../lib/format'
import { useAccounts } from '../../accounts/queries'
import CategorySpending from '../components/CategorySpending'
import MonthlyChart from '../components/MonthlyChart'
import TopExpenses from '../components/TopExpenses'
import { useCategorySpending, useMonthlySummary, useTopExpenses } from '../queries'

const periodOptions = [
  { value: '1', label: 'Ez a hónap' },
  { value: '3', label: '3 hónap' },
  { value: '6', label: '6 hónap' },
  { value: '12', label: '1 év' },
] as const

type PeriodValue = (typeof periodOptions)[number]['value']

const percentFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 0 })

// A kiválasztott hónapok összesítése a havi adatokból
function totals(months: MonthlySummaryResponse[]) {
  const income = months.reduce((sum, month) => sum + month.income, 0)
  const outcome = months.reduce((sum, month) => sum + month.outcome, 0)
  return { income, outcome, net: income - outcome, savingRate: income > 0 ? ((income - outcome) / income) * 100 : undefined }
}

// Statisztika: egy szűrősor szűr mindent, a címsorban tárolva (?months=6&currency=HUF)
export default function StatisticsPage() {
  const [params, setParams] = useSearchParams()
  const accountsQuery = useAccounts()
  const accounts = accountsQuery.data ?? []
  const [today] = useState(() => new Date())

  const available = currencies.filter((currency) => accounts.some((account) => account.currency === currency && account.status !== 'CLOSED'))
  const requestedCurrency = params.get('currency')
  const currency = available.find((item) => item === requestedCurrency) ?? available[0]
  const period: PeriodValue = periodOptions.some((option) => option.value === params.get('months')) ? (params.get('months') as PeriodValue) : '6'
  const months = Number(period)

  // Közös időszak a grafikonnak és a kategóriáknak: az utolsó N naptári hónap
  const from = toIsoDate(addMonths(today, -(months - 1)))
  const to = toIsoDate(today)
  const periodLabel = `${formatIsoDate(from)} – ${formatIsoDate(to)}`

  const monthlyQuery = useMonthlySummary(currency, months)
  const categoriesQuery = useCategorySpending(currency, from, to)
  const topQuery = useTopExpenses(currency, from, to, 6)

  function update(key: 'months' | 'currency', value: string) {
    setParams((current) => {
      const next = new URLSearchParams(current)
      next.set(key, value)
      return next
    })
  }

  const summary = monthlyQuery.data ? totals(monthlyQuery.data) : undefined
  const tiles = summary
    ? [
        { label: 'Bevétel', value: formatMoney(summary.income, currency ?? 'HUF'), icon: ArrowDownLeft, iconClass: 'text-chart-income' },
        { label: 'Kiadás', value: formatMoney(summary.outcome, currency ?? 'HUF'), icon: ArrowUpRight, iconClass: 'text-chart-outcome' },
        { label: 'Különbözet', value: formatSignedMoney(summary.net, currency ?? 'HUF'), icon: Scale, iconClass: 'text-muted-foreground' },
        {
          label: 'Megtakarítási arány',
          value: summary.savingRate !== undefined ? `${percentFormatter.format(summary.savingRate)}%` : '–',
          icon: PiggyBank,
          iconClass: 'text-muted-foreground',
          hint: 'A bevétel hányad része maradt meg',
        },
      ]
    : undefined

  // A pénznem a számlákból jön: nélkülük a többi lekérdezés el sem indulna (örök váz helyett hiba)
  if (accountsQuery.isError) {
    return (
      <>
        <PageHeader title="Statisztika" description="Mire megy el a pénzed?" />
        <Card>
          <QueryError message={getErrorMessage(accountsQuery.error)} retrying={accountsQuery.isFetching} onRetry={() => void accountsQuery.refetch()} />
        </Card>
      </>
    )
  }

  return (
    <>
      <PageHeader title="Statisztika" description="Mire megy el a pénzed?" />

      <Card className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between sm:p-5">
        <ChipGroup label="Időszak" value={period} options={[...periodOptions]} onChange={(value) => update('months', value)} />
        {available.length > 1 && currency && (
          <SegmentedControl
            label="Pénznem"
            value={currency}
            onChange={(value) => update('currency', value)}
            options={available.map((item) => ({ value: item, label: currencyName(item) }))}
          />
        )}
      </Card>

      {monthlyQuery.isError ? (
        <Card className="mt-6">
          <QueryError message={getErrorMessage(monthlyQuery.error)} retrying={monthlyQuery.isFetching} onRetry={() => void monthlyQuery.refetch()} />
        </Card>
      ) : (
        <>
          <dl className={cn('mt-6 grid grid-cols-2 gap-3 transition-opacity duration-300 lg:grid-cols-4 lg:gap-6', monthlyQuery.isPlaceholderData && 'opacity-50')}>
            {tiles
              ? tiles.map((tile) => (
                  <Card key={tile.label} className="@container flex flex-col p-4 sm:p-5">
                    <dt className="flex items-center gap-1.5 text-sm font-medium text-muted-foreground" title={tile.hint}>
                      <tile.icon aria-hidden className={cn('size-4', tile.iconClass)} />
                      {tile.label}
                    </dt>
                    <dd className="mt-auto pt-1.5 text-[clamp(1.125rem,12cqi,1.5rem)] font-extrabold tracking-tight break-words text-foreground">{tile.value}</dd>
                  </Card>
                ))
              : [0, 1, 2, 3].map((index) => <SkeletonStatTile key={index} />)}
          </dl>

          <div className="mt-6">
            <MonthlyChart data={monthlyQuery.data} currency={currency ?? 'HUF'} months={months} refreshing={monthlyQuery.isPlaceholderData} />
          </div>
        </>
      )}

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        {categoriesQuery.isError ? (
          <Card>
            <QueryError message={getErrorMessage(categoriesQuery.error)} retrying={categoriesQuery.isFetching} onRetry={() => void categoriesQuery.refetch()} />
          </Card>
        ) : (
          <CategorySpending data={categoriesQuery.data} currency={currency ?? 'HUF'} periodLabel={periodLabel} refreshing={categoriesQuery.isPlaceholderData} />
        )}
        {topQuery.isError ? (
          <Card>
            <QueryError message={getErrorMessage(topQuery.error)} retrying={topQuery.isFetching} onRetry={() => void topQuery.refetch()} />
          </Card>
        ) : (
          <TopExpenses data={topQuery.data} periodLabel={periodLabel} refreshing={topQuery.isPlaceholderData} />
        )}
      </div>
    </>
  )
}
