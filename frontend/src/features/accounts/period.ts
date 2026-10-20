import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { addDays, addMonths, endOfMonth, startOfMonth, toIsoDate } from '../../lib/dates'

// A számlaadatlap időszaka: ez szűri az egyenleggrafikont és a kivonatot is. A címsorban tárolódik
// (?period=90d vagy ?from=2026-09-01&to=2026-09-15), így megosztható, és a Vissza gomb is működik vele.

export type PeriodPreset = '30d' | 'this-month' | 'last-month' | '90d' | '365d'

export interface Period {
  preset: PeriodPreset | 'custom'
  from: string
  to: string
}

export const periodPresets: { value: PeriodPreset; label: string }[] = [
  { value: '30d', label: '30 nap' },
  { value: 'this-month', label: 'Ez a hónap' },
  { value: 'last-month', label: 'Előző hónap' },
  { value: '90d', label: '3 hónap' },
  { value: '365d', label: '1 év' },
]

const DEFAULT_PRESET: PeriodPreset = '30d'
const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/

export function presetRange(preset: PeriodPreset, today: Date): { from: string; to: string } {
  switch (preset) {
    case 'this-month':
      return { from: toIsoDate(startOfMonth(today)), to: toIsoDate(today) }
    case 'last-month': {
      const start = addMonths(today, -1)
      return { from: toIsoDate(start), to: toIsoDate(endOfMonth(start)) }
    }
    case '90d':
      return { from: toIsoDate(addDays(today, -89)), to: toIsoDate(today) }
    case '365d':
      return { from: toIsoDate(addDays(today, -364)), to: toIsoDate(today) }
    default:
      return { from: toIsoDate(addDays(today, -29)), to: toIsoDate(today) }
  }
}

function isPreset(value: string | null): value is PeriodPreset {
  return periodPresets.some((preset) => preset.value === value)
}

function parsePeriod(params: URLSearchParams, today: Date): Period {
  const from = params.get('from')
  const to = params.get('to')
  // Egyedi időszak: két érvényes dátum, a kezdő nem későbbi a zárónál, és a záró nem a jövőben van
  if (from && to && ISO_DATE.test(from) && ISO_DATE.test(to) && from <= to && to <= toIsoDate(today)) {
    return { preset: 'custom', from, to }
  }
  const preset = params.get('period')
  const selected = isPreset(preset) ? preset : DEFAULT_PRESET
  return { preset: selected, ...presetRange(selected, today) }
}

export function usePeriod() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [today] = useState(() => new Date())
  const period = parsePeriod(searchParams, today)

  function selectPreset(preset: PeriodPreset) {
    setSearchParams(
      (current) => {
        const next = new URLSearchParams(current)
        next.set('period', preset)
        next.delete('from')
        next.delete('to')
        return next
      },
      { replace: true },
    )
  }

  function selectRange(from: string, to: string) {
    setSearchParams(
      (current) => {
        const next = new URLSearchParams(current)
        next.delete('period')
        next.set('from', from)
        next.set('to', to)
        return next
      },
      { replace: true },
    )
  }

  return { period, today: toIsoDate(today), selectPreset, selectRange }
}
