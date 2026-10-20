import { Check } from 'lucide-react'
import type { AccountResponse } from '../../../api/types'
import { cn } from '../../../lib/cn'
import { formatMoney, formatShortIban } from '../../../lib/format'
import { accountTheme } from '../../accounts/accountTheme'

interface AccountOptionProps {
  account: AccountResponse
  selected: boolean
  onSelect: () => void
  // Címzettként (saját számlára utalásnál) nem az egyenleg a lényeg
  showBalance?: boolean
}

// Választható számla (rádiógombként): a számla színvilágának csíkja, neve, rövidített számlaszáma és egyenlege
export default function AccountOption({ account, selected, onSelect, showBalance = true }: AccountOptionProps) {
  return (
    <button
      type="button"
      role="radio"
      aria-checked={selected}
      onClick={onSelect}
      className={cn(
        'relative flex w-full items-center gap-4 overflow-hidden rounded-2xl border p-4 pl-6 text-left transition duration-200',
        'focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none',
        selected ? 'border-brand-400 bg-brand-50 dark:border-brand-500/60 dark:bg-brand-950/60' : 'border-border bg-card/70 hover:border-brand-300',
      )}
    >
      <span aria-hidden className={cn('absolute inset-y-0 left-0 w-1.5', accountTheme(account).surface)} />
      <span className="min-w-0 flex-1">
        <span className="block truncate font-bold text-foreground">{account.name}</span>
        <span className="block font-mono text-sm tracking-wide text-muted-foreground">{formatShortIban(account.accountNumber)}</span>
      </span>
      {showBalance && <span className="shrink-0 font-bold text-foreground tabular-nums">{formatMoney(account.balance, account.currency)}</span>}
      <span
        aria-hidden
        className={cn(
          'flex size-6 shrink-0 items-center justify-center rounded-full border-2 transition',
          selected ? 'border-transparent bg-brand-gradient text-white' : 'border-border',
        )}
      >
        {selected && <Check className="size-3.5" />}
      </span>
    </button>
  )
}
