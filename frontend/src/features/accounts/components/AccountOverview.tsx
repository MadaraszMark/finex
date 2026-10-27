import { ArrowRight, CreditCard } from 'lucide-react'
import { Link } from 'react-router'
import type { AccountResponse, CardResponse } from '../../../api/types'
import AnimatedNumber from '../../../components/ui/AnimatedNumber'
import Badge, { type BadgeTone } from '../../../components/ui/Badge'
import { Card } from '../../../components/ui/Card'
import CopyButton from '../../../components/ui/CopyButton'
import { accountStatusLabels, cardStatusLabels, formatIban, formatMaskedCard, formatMoney, formatShortDate } from '../../../lib/format'
import { accountTheme } from '../accountTheme'

const statusTones: Record<AccountResponse['status'], BadgeTone> = {
  ACTIVE: 'success',
  BLOCKED: 'danger',
  FROZEN: 'warning',
  CLOSED: 'neutral',
}

interface AccountOverviewProps {
  account: AccountResponse
  card: CardResponse | undefined
  now: Date
}

// A számlaadatlap felső kártyája: egyenleg, állapot, számlaszám és a hozzá tartozó bankkártya
export default function AccountOverview({ account, card, now }: AccountOverviewProps) {
  const theme = accountTheme(account)

  return (
    <Card className="relative overflow-hidden p-6 sm:p-8">
      <div aria-hidden className={`absolute inset-y-0 left-0 w-1.5 ${theme.surface}`} />

      <div className="flex flex-col gap-8 lg:flex-row lg:items-end lg:justify-between">
        <div className="min-w-0">
          <p className="text-sm font-semibold text-muted-foreground">Egyenleg</p>
          <AnimatedNumber
            value={account.balance}
            format={(value) => formatMoney(value, account.currency)}
            className="mt-1 block text-4xl font-extrabold tracking-tight break-words text-foreground sm:text-5xl"
          />
          <div className="mt-3 flex flex-wrap items-center gap-x-3 gap-y-2 text-sm text-muted-foreground">
            <Badge tone={statusTones[account.status]}>{accountStatusLabels[account.status]}</Badge>
            <span>Nyitva: {formatShortDate(new Date(account.createdAt), now)}</span>
          </div>
        </div>

        <dl className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:w-[30rem] lg:shrink-0">
          <div className="sm:col-span-2">
            <dt className="text-sm font-medium text-muted-foreground">Számlaszám (IBAN)</dt>
            <dd className="mt-1 flex items-center gap-2">
              <span className="font-mono text-[15px] font-semibold tracking-wide break-all text-foreground">{formatIban(account.accountNumber)}</span>
              <CopyButton value={account.accountNumber} label="Számlaszám másolása" successMessage="A számlaszámot a vágólapra másoltuk." className="size-9 shrink-0" />
            </dd>
          </div>
          <div className="sm:col-span-2">
            <dt className="text-sm font-medium text-muted-foreground">Bankkártya</dt>
            <dd className="mt-1">
              {card ? (
                <Link
                  to={`/cards?card=${card.id}`}
                  className="group inline-flex items-center gap-2.5 rounded-xl font-semibold text-foreground transition hover:text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
                >
                  <CreditCard aria-hidden className="size-5 text-primary" />
                  <span className="font-mono tracking-wider">{formatMaskedCard(card.maskedNumber)}</span>
                  <span className="text-sm font-medium text-muted-foreground">· {cardStatusLabels[card.status]}</span>
                  <ArrowRight aria-hidden className="size-4 transition-transform group-hover:translate-x-1" />
                </Link>
              ) : (
                <span className="text-muted-foreground">Nincs kártya</span>
              )}
            </dd>
          </div>
        </dl>
      </div>
    </Card>
  )
}
