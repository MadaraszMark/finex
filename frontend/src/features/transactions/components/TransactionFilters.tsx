import { Search, SlidersHorizontal, X } from 'lucide-react'
import { useEffect, useId, useState } from 'react'
import type { AccountResponse, CategoryResponse, TransactionSearchParams, TransactionType } from '../../../api/types'
import Button from '../../../components/ui/Button'
import ChipGroup from '../../../components/ui/ChipGroup'
import Select from '../../../components/ui/Select'
import TextField from '../../../components/ui/TextField'
import { cn } from '../../../lib/cn'
import { addDays, addMonths, endOfMonth, startOfMonth, toIsoDate } from '../../../lib/dates'
import { transactionTypeLabels } from '../../../lib/format'
import type { FilterParam } from '../filters'

type TypeOption = TransactionType | 'ALL'
type PeriodOption = 'all' | '30d' | 'this-month' | 'last-month' | '365d'

const typeOptions: { value: TypeOption; label: string }[] = [
  { value: 'ALL', label: 'Mind' },
  ...(Object.keys(transactionTypeLabels) as TransactionType[]).map((type) => ({ value: type, label: transactionTypeLabels[type] })),
]

const periodOptions: { value: PeriodOption; label: string }[] = [
  { value: 'all', label: 'Bármikor' },
  { value: '30d', label: '30 nap' },
  { value: 'this-month', label: 'Ez a hónap' },
  { value: 'last-month', label: 'Előző hónap' },
  { value: '365d', label: '1 év' },
]

function periodRange(period: PeriodOption, today: Date): { from?: string; to?: string } {
  switch (period) {
    case '30d':
      return { from: toIsoDate(addDays(today, -29)), to: toIsoDate(today) }
    case 'this-month':
      return { from: toIsoDate(startOfMonth(today)), to: toIsoDate(today) }
    case 'last-month': {
      const start = addMonths(today, -1)
      return { from: toIsoDate(start), to: toIsoDate(endOfMonth(start)) }
    }
    case '365d':
      return { from: toIsoDate(addDays(today, -364)), to: toIsoDate(today) }
    default:
      return {}
  }
}

const inputClass =
  'h-11 w-full rounded-xl border border-border bg-card/80 px-3 text-sm text-foreground transition focus:border-brand-400 focus:ring-4 focus:ring-ring/20 focus:outline-none dark:[color-scheme:dark]'

interface TransactionFiltersProps {
  filter: TransactionSearchParams
  activeCount: number
  accounts: AccountResponse[]
  categories: CategoryResponse[]
  onChange: (patch: Partial<Record<FilterParam, string | undefined>>, options?: { replace?: boolean }) => void
  onClear: () => void
}

// Szűrők; a ritkábbak a "Részletes szűrés" alatt, hogy mobilon ne foglaljanak egy képernyőt
export default function TransactionFilters({ filter, activeCount, accounts, categories, onChange, onClear }: TransactionFiltersProps) {
  const detailsId = useId()
  const [today] = useState(() => new Date())
  const [detailsOpen, setDetailsOpen] = useState(
    () => filter.accountId !== undefined || filter.categoryId !== undefined || filter.minAmount !== undefined || filter.maxAmount !== undefined,
  )

  // A kereső szövege 300 ms szünet után kerül a címsorba (nem minden leütésre keres)
  const [searchText, setSearchText] = useState(filter.search ?? '')
  const [syncedSearch, setSyncedSearch] = useState(filter.search ?? '')
  if ((filter.search ?? '') !== syncedSearch) {
    // A címsor kívülről változott (pl. Vissza gomb): a mező kövesse
    setSyncedSearch(filter.search ?? '')
    setSearchText(filter.search ?? '')
  }

  useEffect(() => {
    if (searchText.trim() === (filter.search ?? '')) {
      return
    }
    const timer = window.setTimeout(() => onChange({ q: searchText.trim() || undefined }, { replace: true }), 300)
    return () => window.clearTimeout(timer)
  }, [searchText, filter.search, onChange])

  const period = (periodOptions.find((option) => {
    const range = periodRange(option.value, today)
    return range.from === filter.from && range.to === filter.to
  })?.value ?? undefined) as PeriodOption | undefined

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
        <TextField
          label="Keresés"
          type="search"
          placeholder="Partner vagy közlemény, pl. Tesco"
          leftIcon={<Search />}
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
          className="sm:flex-1"
        />
        <div className="flex gap-2">
          <Button
            variant="outline"
            leftIcon={<SlidersHorizontal />}
            aria-expanded={detailsOpen}
            aria-controls={detailsId}
            onClick={() => setDetailsOpen((open) => !open)}
            className="h-13"
          >
            Részletes szűrés
          </Button>
          {activeCount > 0 && (
            <Button variant="ghost" leftIcon={<X />} onClick={onClear} className="h-13">
              Törlés ({activeCount})
            </Button>
          )}
        </div>
      </div>

      <div className="flex flex-col gap-3 2xl:flex-row 2xl:items-center 2xl:justify-between">
        <ChipGroup label="Típus" value={filter.type ?? 'ALL'} options={typeOptions} onChange={(type) => onChange({ type: type === 'ALL' ? undefined : type })} />
        <ChipGroup
          label="Időszak"
          value={period}
          options={periodOptions}
          onChange={(value) => {
            const range = periodRange(value, today)
            onChange({ from: range.from, to: range.to })
          }}
        />
      </div>

      <div id={detailsId} hidden={!detailsOpen} className={cn('grid grid-cols-1 gap-4 rounded-2xl border border-border bg-card/60 p-4 sm:grid-cols-2 lg:grid-cols-4')}>
        <Select label="Számla" value={filter.accountId ?? ''} onChange={(event) => onChange({ account: event.target.value || undefined })}>
          <option value="">Összes számla</option>
          {accounts.map((account) => (
            <option key={account.id} value={account.id}>
              {account.name} ({account.currency})
            </option>
          ))}
        </Select>
        <Select label="Kategória" value={filter.categoryId ?? ''} onChange={(event) => onChange({ category: event.target.value || undefined })}>
          <option value="">Összes kategória</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </Select>
        <fieldset>
          <legend className="mb-1.5 text-sm font-medium text-foreground">Időszak (egyedi)</legend>
          <div className="flex items-center gap-2">
            <input
              type="date"
              aria-label="Kezdő nap"
              value={filter.from ?? ''}
              max={filter.to ?? toIsoDate(today)}
              onChange={(event) => onChange({ from: event.target.value || undefined })}
              className={inputClass}
            />
            <span aria-hidden className="text-muted-foreground">
              –
            </span>
            <input
              type="date"
              aria-label="Záró nap"
              value={filter.to ?? ''}
              min={filter.from}
              max={toIsoDate(today)}
              onChange={(event) => onChange({ to: event.target.value || undefined })}
              className={inputClass}
            />
          </div>
        </fieldset>
        {/* A mezők a kilépéskor szűrnek; a kulcs miatt a szűrők törlésekor ürülnek ki */}
        <fieldset key={`${filter.minAmount ?? ''}-${filter.maxAmount ?? ''}`}>
          <legend className="mb-1.5 text-sm font-medium text-foreground">Összeg</legend>
          <div className="flex items-center gap-2">
            <input
              type="number"
              inputMode="decimal"
              min={0}
              aria-label="Legalább"
              placeholder="min."
              defaultValue={filter.minAmount}
              onBlur={(event) => onChange({ min: event.target.value || undefined })}
              className={inputClass}
            />
            <span aria-hidden className="text-muted-foreground">
              –
            </span>
            <input
              type="number"
              inputMode="decimal"
              min={0}
              aria-label="Legfeljebb"
              placeholder="max."
              defaultValue={filter.maxAmount}
              onBlur={(event) => onChange({ max: event.target.value || undefined })}
              className={inputClass}
            />
          </div>
        </fieldset>
      </div>
    </div>
  )
}
