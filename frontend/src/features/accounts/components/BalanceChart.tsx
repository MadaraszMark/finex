import { useReducedMotion } from 'motion/react'
import { Area, AreaChart, CartesianGrid, ReferenceDot, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import ChartTooltip from '../../../components/charts/ChartTooltip'
import { cn } from '../../../lib/cn'
import { formatCompactNumber, formatMoney } from '../../../lib/format'

export interface BalancePoint {
  time: number
  balance: number
}

interface BalanceChartProps {
  points: BalancePoint[]
  currency: string
  from: number
  to: number
  // Időszakváltás közben az előző grafikon halványítva marad (nincs villanás)
  refreshing?: boolean
}

const DAY = 24 * 60 * 60 * 1000
const dayFormatter = new Intl.DateTimeFormat('hu-HU', { month: 'short', day: 'numeric' })
const monthFormatter = new Intl.DateTimeFormat('hu-HU', { month: 'short' })
const tooltipDateFormatter = new Intl.DateTimeFormat('hu-HU', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
const axisTick = { fill: 'var(--muted-foreground)', fontSize: 12 }

// Egyenletesen elosztott dátumfeliratok az időtengelyen
function evenTicks(from: number, to: number, count: number): number[] {
  const step = (to - from) / (count - 1)
  return Array.from({ length: count }, (_, index) => Math.round(from + index * step))
}

// Lépcsős egyenleggörbe: az egyenleg csak a tételeknél ugrik, közben nem változik
export default function BalanceChart({ points, currency, from, to, refreshing = false }: BalanceChartProps) {
  const reduceMotion = useReducedMotion()
  const formatTick = (value: number) => (to - from > 100 * DAY ? monthFormatter : dayFormatter).format(value)
  const last = points.at(-1)

  return (
    <div className={cn('h-64 transition-opacity duration-300', refreshing && 'opacity-50')}>
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={points} margin={{ top: 30, right: 24, bottom: 0, left: 0 }}>
          <CartesianGrid vertical={false} stroke="var(--chart-grid)" />
          <XAxis
            dataKey="time"
            type="number"
            scale="time"
            domain={[from, to]}
            ticks={evenTicks(from, to, 5)}
            tickFormatter={formatTick}
            tickLine={false}
            axisLine={false}
            tick={axisTick}
            tickMargin={10}
          />
          <YAxis width={56} domain={['auto', 'auto']} tickFormatter={formatCompactNumber} tickLine={false} axisLine={false} tick={axisTick} />
          <Tooltip
            cursor={{ stroke: 'var(--muted-foreground)', strokeOpacity: 0.4, strokeWidth: 1 }}
            content={({ active, payload }) => {
              const point = payload?.[0]?.payload as BalancePoint | undefined
              return active && point ? (
                <ChartTooltip
                  title={tooltipDateFormatter.format(point.time)}
                  rows={[{ key: 'balance', label: 'Egyenleg', value: formatMoney(point.balance, currency), color: 'var(--chart-balance)' }]}
                />
              ) : null
            }}
          />
          <Area
            type="stepAfter"
            dataKey="balance"
            stroke="var(--chart-balance)"
            strokeWidth={2}
            strokeLinejoin="round"
            fill="var(--chart-balance)"
            fillOpacity={0.1}
            baseValue="dataMin"
            dot={false}
            activeDot={{ r: 5, fill: 'var(--chart-balance)', stroke: 'var(--card)', strokeWidth: 2 }}
            isAnimationActive={!reduceMotion}
          />
          {last && (
            <ReferenceDot
              x={last.time}
              y={last.balance}
              r={5}
              fill="var(--chart-balance)"
              stroke="var(--card)"
              strokeWidth={2}
              label={({ viewBox }: { viewBox?: { x?: number; y?: number; width?: number } }) => (
                <text
                  x={(viewBox?.x ?? 0) + (viewBox?.width ?? 0)}
                  y={(viewBox?.y ?? 0) - 10}
                  textAnchor="end"
                  fill="var(--foreground)"
                  fontSize={12}
                  fontWeight={700}
                >
                  {formatMoney(last.balance, currency)}
                </text>
              )}
            />
          )}
        </AreaChart>
      </ResponsiveContainer>
    </div>
  )
}
