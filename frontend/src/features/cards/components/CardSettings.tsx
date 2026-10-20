import { ShoppingBag } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse, CardResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Badge, { type BadgeTone } from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import Meter from '../../../components/ui/Meter'
import Switch from '../../../components/ui/Switch'
import { cardStatusLabels, formatCardExpiry, formatIsoDate, formatMaskedCard, formatMoney, formatShortIban } from '../../../lib/format'
import { useBlockCard, useUnblockCard, useUpdateCardLimits } from '../queries'
import CardPaymentDialog from './CardPaymentDialog'
import LimitForm from './LimitForm'

const statusTones: Record<CardResponse['status'], BadgeTone> = {
  ACTIVE: 'success',
  BLOCKED: 'danger',
  CANCELLED: 'neutral',
}

interface CardSettingsProps {
  card: CardResponse
  account: AccountResponse | undefined
}

// A kiválasztott kártya kezelése: mai költés a limithez képest, tiltás és feloldás, online fizetés, napi limit,
// alapadatok és demó fizetés. A megszűnt kártya (lezárt számla) csak megtekinthető.
export default function CardSettings({ card, account }: CardSettingsProps) {
  const blockCard = useBlockCard()
  const unblockCard = useUnblockCard()
  const updateLimits = useUpdateCardLimits()
  const [paying, setPaying] = useState(false)

  const currency = account?.currency ?? 'HUF'
  const cancelled = card.status === 'CANCELLED'
  const blocked = card.status === 'BLOCKED'
  const remaining = Math.max(card.dailyLimit - card.spentToday, 0)

  function setBlocked(nextBlocked: boolean) {
    const mutation = nextBlocked ? blockCard : unblockCard
    mutation.mutate(card.id, {
      onSuccess: () =>
        toast.success(nextBlocked ? 'Letiltottad a kártyát' : 'Feloldottad a kártyát', {
          description: nextBlocked ? 'Amíg le van tiltva, nem lehet vele fizetni.' : 'A kártya újra használható.',
        }),
      onError: (error) => toast.error(getErrorMessage(error)),
    })
  }

  function setOnlineEnabled(enabled: boolean) {
    updateLimits.mutate(
      { id: card.id, request: { dailyLimit: card.dailyLimit, onlinePaymentEnabled: enabled } },
      {
        onSuccess: () => toast.success(enabled ? 'Bekapcsoltad az online fizetést' : 'Kikapcsoltad az online fizetést'),
        onError: (error) => toast.error(getErrorMessage(error)),
      },
    )
  }

  return (
    <Card className="p-6 sm:p-8">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <h2 className="text-xl font-bold tracking-tight text-foreground">{account?.name ?? 'Bankkártya'}</h2>
          <p className="mt-0.5 font-mono text-sm tracking-wider text-muted-foreground">{formatMaskedCard(card.maskedNumber)}</p>
        </div>
        <Badge tone={statusTones[card.status]}>{cardStatusLabels[card.status]}</Badge>
      </div>

      {cancelled && (
        <Alert tone="info" className="mt-5">
          Ez a kártya megszűnt, mert a hozzá tartozó számlát lezártad. A beállításai már nem módosíthatók.
        </Alert>
      )}

      <section aria-labelledby="spent-today" className="mt-6 rounded-2xl bg-muted/70 p-4">
        <div className="flex items-baseline justify-between gap-3">
          <h3 id="spent-today" className="text-sm font-medium text-muted-foreground">
            Mai költés
          </h3>
          <p className="text-sm text-muted-foreground">
            <span className="font-bold text-foreground">{formatMoney(card.spentToday, currency)}</span> / {formatMoney(card.dailyLimit, currency)}
          </p>
        </div>
        <Meter
          className="mt-3"
          label="Mai költés a napi limithez képest"
          value={card.spentToday}
          max={card.dailyLimit}
          valueText={`${formatMoney(card.spentToday, currency)} a ${formatMoney(card.dailyLimit, currency)} napi limitből`}
        />
        <p className="mt-2 text-sm text-muted-foreground">
          Ma még <span className="font-semibold text-foreground">{formatMoney(remaining, currency)}</span> költhető.
        </p>
      </section>

      <div className="mt-6 divide-y divide-border">
        <Switch
          className="pb-5"
          label="Kártya letiltása"
          description={blocked ? 'A kártya le van tiltva, nem lehet vele fizetni. Bármikor feloldhatod.' : 'Elveszett a kártyád? Tiltsd le egy érintéssel.'}
          checked={blocked || cancelled}
          tone="danger"
          disabled={cancelled}
          loading={blockCard.isPending || unblockCard.isPending}
          onCheckedChange={setBlocked}
        />
        <Switch
          className="py-5"
          label="Online fizetés"
          description="Webáruházban és alkalmazásokban is lehet fizetni a kártyával."
          checked={card.onlinePaymentEnabled}
          disabled={cancelled}
          loading={updateLimits.isPending}
          onCheckedChange={setOnlineEnabled}
        />
        <div className="pt-5">
          <LimitForm card={card} currency={currency} disabled={cancelled} />
        </div>
      </div>

      <dl className="mt-6 grid grid-cols-1 gap-4 rounded-2xl border border-border p-4 text-sm sm:grid-cols-2">
        <div>
          <dt className="text-muted-foreground">Számla</dt>
          <dd className="mt-0.5 font-semibold text-foreground">
            {account ? (
              <Link to={`/accounts/${account.id}`} className="rounded-md transition hover:text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none">
                {account.name} · <span className="font-mono">{formatShortIban(account.accountNumber)}</span>
              </Link>
            ) : (
              '–'
            )}
          </dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Kártyabirtokos</dt>
          <dd className="mt-0.5 font-semibold text-foreground">{card.holderName}</dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Lejárat</dt>
          <dd className="mt-0.5 font-semibold text-foreground">{formatCardExpiry(card.expiryDate)}</dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Érvényes eddig</dt>
          <dd className="mt-0.5 font-semibold text-foreground">{formatIsoDate(card.expiryDate)}</dd>
        </div>
      </dl>

      <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-muted-foreground">Próbáld ki: a fizetés levonódik a számláról, és beleszámít a mai költésbe.</p>
        <Button leftIcon={<ShoppingBag />} className="shrink-0" disabled={card.status !== 'ACTIVE'} onClick={() => setPaying(true)}>
          Demó fizetés
        </Button>
      </div>

      <CardPaymentDialog card={card} currency={currency} open={paying} onClose={() => setPaying(false)} />
    </Card>
  )
}
