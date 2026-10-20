import { Zap } from 'lucide-react'
import type { AccountResponse } from '../../../api/types'
import { cn } from '../../../lib/cn'
import { formatIban, formatMoney, parseAmount } from '../../../lib/format'
import { isValidIban, normalizeIban } from '../../../lib/iban'

interface TransferOverviewProps {
  source: AccountResponse | undefined
  toAccountNumber: string
  partnerName: string
  amount: string
  message: string
  categoryName: string | undefined
  // true: FineX-es számla (azonnal megérkezik), false: másik bank, undefined: nem tudjuk előre
  internal: boolean | undefined
  compact?: boolean
}

// Az utalás adatai egy listában: az áttekintés lépésben és (nagy képernyőn) gépelés közben is, oldalt
export default function TransferOverview({ source, toAccountNumber, partnerName, amount, message, categoryName, internal, compact = false }: TransferOverviewProps) {
  const currency = source?.currency ?? 'HUF'
  const parsedAmount = parseAmount(amount)
  const iban = normalizeIban(toAccountNumber)

  const rows = [
    { label: 'Honnan', value: source ? `${source.name} (${formatMoney(source.balance, currency)})` : undefined },
    { label: 'Kinek', value: partnerName.trim() || undefined },
    { label: 'Számlaszám', value: isValidIban(iban) ? formatIban(iban) : undefined, mono: true },
    { label: 'Közlemény', value: message.trim() || undefined },
    { label: 'Kategória', value: categoryName },
  ]

  return (
    <div>
      <p className="text-sm font-medium text-muted-foreground">Összeg</p>
      <p className={cn('font-extrabold tracking-tight break-words text-foreground', compact ? 'text-2xl' : 'text-4xl')}>
        {parsedAmount !== undefined ? formatMoney(parsedAmount, currency) : '–'}
      </p>

      <dl className="mt-4 divide-y divide-border rounded-2xl border border-border">
        {rows.map((row) => (
          <div key={row.label} className="flex flex-col gap-0.5 px-4 py-3">
            <dt className="text-xs font-medium text-muted-foreground">{row.label}</dt>
            <dd className={cn('text-sm font-semibold break-all', row.value ? 'text-foreground' : 'text-muted-foreground', row.mono && row.value && 'font-mono tracking-wide')}>
              {row.value ?? '–'}
            </dd>
          </div>
        ))}
      </dl>

      {iban && isValidIban(iban) && (
        <p className="mt-4 flex items-start gap-2.5 rounded-2xl bg-muted/70 px-4 py-3 text-sm text-muted-foreground">
          <Zap aria-hidden className="mt-0.5 size-4 shrink-0 text-primary" />
          {internal === true
            ? 'FineX-es számla: az összeg azonnal megérkezik.'
            : internal === false
              ? 'Másik bank számlája: a terhelés azonnal megtörténik a számládon.'
              : 'Ha a címzett számlája FineX-es, az összeg azonnal megérkezik.'}
        </p>
      )}
    </div>
  )
}
