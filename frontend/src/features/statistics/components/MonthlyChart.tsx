import { ChartColumn, Table } from 'lucide-react'
import { useReducedMotion } from 'motion/react'
import { useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { MonthlySummaryResponse } from '../../../api/types'
import ChartTooltip from '../../../components/charts/ChartTooltip'
import { Card, CardHeader } from '../../../components/ui/Card'
import SegmentedControl from '../../../components/ui/SegmentedControl'
import Skeleton from '../../../components/ui/Skeleton'
import { cn } from '../../../lib/cn'
import { currencyName, formatCompactNumber, formatMoney, formatMonth, formatSignedMoney } from '../../../lib/format'

type View = 'chart' | 'table'

// A két sorozat (bevétel, kiadás) színei a dataviz-validátorral ellenőrzöttek; a jelmagyarázat mindig látszik
const series = [
  { key: 'income', label: 'Bevétel', color: 'var(--chart-income)', swatch: 'bg-chart-income' },
  { key: 'outcome', label: 'Kiadás', color: 'var(--chart-outcome)', swatch: 'bg-chart-outcome' },
] as const

const axisTick = { fill: 'var(--muted-foreground)', fontSize: 12 }

interface MonthlyChartProps {
  data: MonthlySummaryResponse[] | undefined
  currency: string
  // Hány hónapot mutat (a felirathoz; az adatot a hívó kéri le)
  months?: number
  refreshing?: boolean
}

function periodText(months: number): string {
  return months === 1 ? 'Ez a hónap' : `Az elmúlt ${months} hónap`
}

// Havi bevétel és kiadás csoportosított oszlopokban (vékony oszlopok, lekerekített tető, 2 px rés a párok között),
// buborékkal és táblázatos nézettel (ugyanazok az adatok akadálymentesen, szövegként)
export default function MonthlyChart({ data, currency, months = 6, refreshing = false }: MonthlyChartProps) {
  const reduceMotion = useReducedMotion()
  const [view, setView] = useState<View>('chart')
  const empty = data?.every((month) => month.income === 0 && month.outcome === 0) ?? false

  return (
    // A kártya kitölti a rendelkezésére álló magasságot (pl. a főoldalon a mellette álló kártyáéhoz igazodik), a grafikon
    // legalább 15rem magas
    <Card className="flex flex-col p-5 sm:p-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <CardHeader title="Bevétel és kiadás" description={`${periodText(months)} · ${currencyName(currency)}`} />
        <SegmentedControl
          label="Megjelenítés"
          value={view}
          onChange={setView}
          options={[
            { value: 'chart', label: 'Grafikon', icon: <ChartColumn /> },
            { value: 'table', label: 'Táblázat', icon: <Table /> },
          ]}
        />
      </div>

      <ul className="mt-4 flex flex-wrap gap-x-5 gap-y-1 text-sm text-muted-foreground">
        {series.map((item) => (
          <li key={item.key} className="flex items-center gap-2">
            <span aria-hidden className={cn('size-3 rounded-[3px]', item.swatch)} />
            {item.label}
          </li>
        ))}
      </ul>

      <div className={cn('mt-4 flex flex-1 flex-col transition-opacity duration-300', refreshing && 'opacity-50')}>
        {!data ? (
          <Skeleton className="h-60" />
        ) : empty ? (
          <p className="flex min-h-60 flex-1 items-center justify-center text-center text-sm text-muted-foreground">Az elmúlt hónapokban nem volt forgalom ebben a pénznemben.</p>
        ) : view === 'chart' ? (
          // Rögzített 15rem-es alapmagasság (a ResponsiveContainer 100%-a ehhez képest számol); ha a kártya magasabb
          // (pl. a főoldalon a mellette álló kártyához nyúlik), a grafikon kitölti a többletet is
          <div className="h-60 grow">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data} barGap={2} barCategoryGap="28%" margin={{ top: 4, right: 4, bottom: 0, left: 0 }}>
                <CartesianGrid vertical={false} stroke="var(--chart-grid)" />
                <XAxis dataKey="month" tickFormatter={(month: string) => formatMonth(month)} tickLine={false} axisLine={false} tick={axisTick} tickMargin={10} />
                <YAxis width={52} tickFormatter={formatCompactNumber} tickLine={false} axisLine={false} tick={axisTick} />
                <Tooltip
                  cursor={{ fill: 'var(--muted)', opacity: 0.7 }}
                  content={({ active, payload }) => {
                    const month = payload?.[0]?.payload as MonthlySummaryResponse | undefined
                    return active && month ? (
                      <ChartTooltip
                        title={formatMonth(month.month, 'long')}
                        rows={series.map((item) => ({ key: item.key, label: item.label, value: formatMoney(month[item.key], currency), color: item.color }))}
                      />
                    ) : null
                  }}
                />
                {series.map((item) => (
                  <Bar
                    key={item.key}
                    dataKey={item.key}
                    name={item.label}
                    fill={item.color}
                    radius={[4, 4, 0, 0]}
                    maxBarSize={24}
                    isAnimationActive={!reduceMotion}
                  />
                ))}
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <table className="w-full text-sm">
            <caption className="sr-only">Havi bevétel és kiadás – {periodText(months).toLowerCase()}</caption>
            <thead>
              <tr className="border-b border-border text-left text-xs font-semibold tracking-wide text-muted-foreground uppercase">
                <th scope="col" className="py-2.5 pr-3 font-semibold">
                  Hónap
                </th>
                <th scope="col" className="py-2.5 pr-3 text-right font-semibold">
                  Bevétel
                </th>
                <th scope="col" className="py-2.5 pr-3 text-right font-semibold">
                  Kiadás
                </th>
                <th scope="col" className="py-2.5 text-right font-semibold">
                  Különbözet
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {data.map((month) => (
                <tr key={month.month}>
                  <th scope="row" className="py-2.5 pr-3 text-left font-medium text-foreground">
                    {formatMonth(month.month, 'long')}
                  </th>
                  <td className="py-2.5 pr-3 text-right text-foreground tabular-nums">{formatMoney(month.income, currency)}</td>
                  <td className="py-2.5 pr-3 text-right text-foreground tabular-nums">{formatMoney(month.outcome, currency)}</td>
                  <td className="py-2.5 text-right font-semibold text-foreground tabular-nums">{formatSignedMoney(month.income - month.outcome, currency)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Card>
  )
}
