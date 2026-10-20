import { CalendarRange } from 'lucide-react'
import ChipGroup from '../../../components/ui/ChipGroup'
import { cn } from '../../../lib/cn'
import { periodPresets, type Period, type PeriodPreset } from '../period'

interface PeriodFilterProps {
  period: Period
  today: string
  onPresetChange: (preset: PeriodPreset) => void
  onRangeChange: (from: string, to: string) => void
  className?: string
}

const dateInputClass =
  'h-10 rounded-xl border border-border bg-card/80 px-3 text-sm text-foreground transition focus:border-brand-400 focus:ring-4 focus:ring-ring/20 focus:outline-none dark:[color-scheme:dark]'

// Az időszak választója egy sorban, a szűrt tartalom (grafikon, kivonat) fölött: gyors időszakok,
// mellettük tetszőleges kezdő és záró nap
export default function PeriodFilter({ period, today, onPresetChange, onRangeChange, className }: PeriodFilterProps) {
  const preset = period.preset === 'custom' ? undefined : period.preset

  return (
    <div className={cn('flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between', className)}>
      <ChipGroup label="Időszak" value={preset} options={periodPresets} onChange={onPresetChange} />

      <fieldset className="flex flex-wrap items-center gap-2 text-sm">
        <legend className="sr-only">Egyedi időszak</legend>
        <CalendarRange aria-hidden className="size-4 text-muted-foreground" />
        <label className="sr-only" htmlFor="period-from">
          Kezdő nap
        </label>
        <input
          id="period-from"
          type="date"
          value={period.from}
          max={period.to}
          onChange={(event) => event.target.value && onRangeChange(event.target.value, period.to)}
          className={dateInputClass}
        />
        <span aria-hidden className="text-muted-foreground">
          –
        </span>
        <label className="sr-only" htmlFor="period-to">
          Záró nap
        </label>
        <input
          id="period-to"
          type="date"
          value={period.to}
          min={period.from}
          max={today}
          onChange={(event) => event.target.value && onRangeChange(period.from, event.target.value)}
          className={dateInputClass}
        />
      </fieldset>
    </div>
  )
}
