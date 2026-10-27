import { ArrowDownLeft, ArrowUpRight, Archive, History, MoreHorizontal, Pencil, PartyPopper, PiggyBank } from 'lucide-react'
import type { SavingsAccountResponse } from '../../../api/types'
import Badge from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import { iconButtonStyles } from '../../../components/ui/buttonStyles'
import Popover from '../../../components/ui/Popover'
import { formatMoney } from '../../../lib/format'
import { estimatedMonthlyInterest } from '../schemas'
import ProgressRing from './ProgressRing'

export type SavingsAction = 'deposit' | 'withdraw' | 'history' | 'edit' | 'close'

const percentFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 2 })
const menuItemClass =
  'flex w-full items-center gap-3 rounded-2xl px-3 py-2.5 text-left text-sm font-semibold text-foreground transition hover:bg-muted [&_svg]:size-5 [&_svg]:text-muted-foreground'

// Egy megtakarítási cél; a ritkább műveletek (mozgások, szerkesztés, lezárás) a "…" menüben
export default function SavingsGoalCard({ savings, onAction }: { savings: SavingsAccountResponse; onAction: (action: SavingsAction) => void }) {
  const active = savings.status === 'ACTIVE'
  const target = savings.targetAmount
  const remaining = target !== null ? Math.max(target - savings.balance, 0) : 0
  const interest = estimatedMonthlyInterest(savings.balance, savings.interestRate)

  return (
    // Konténer: az egyenleg betűmérete a kártya szélességéhez igazodik
    <div className="@container flex h-full flex-col p-5 sm:p-6">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h2 className="truncate text-lg font-bold text-foreground">{savings.name}</h2>
          <p className="text-sm text-muted-foreground">Évi {percentFormatter.format(savings.interestRate)}% kamat, havonta jóváírva</p>
        </div>
        <div className="flex shrink-0 items-center gap-2">
          {!active && <Badge tone="warning">Befagyasztva</Badge>}
          <Popover
            label={`${savings.name}: további műveletek`}
            trigger={<MoreHorizontal className="size-5" />}
            triggerClassName={iconButtonStyles('default', 'size-9')}
            panelClassName="w-60"
          >
            {(close) => (
              <>
                <button
                  type="button"
                  className={menuItemClass}
                  onClick={() => {
                    close()
                    onAction('history')
                  }}
                >
                  <History />
                  Mozgások
                </button>
                <button
                  type="button"
                  className={menuItemClass}
                  disabled={!active}
                  onClick={() => {
                    close()
                    onAction('edit')
                  }}
                >
                  <Pencil />
                  Szerkesztés
                </button>
                <button
                  type="button"
                  className={`${menuItemClass} text-danger [&_svg]:text-danger`}
                  disabled={!active}
                  onClick={() => {
                    close()
                    onAction('close')
                  }}
                >
                  <Archive />
                  Lezárás
                </button>
              </>
            )}
          </Popover>
        </div>
      </div>

      <div className="mt-6 flex items-center gap-5">
        {target !== null ? (
          <ProgressRing percent={savings.progressPercent ?? 0} label={`${savings.name}: a cél teljesítése`} />
        ) : (
          <span className="flex size-28 shrink-0 items-center justify-center rounded-full bg-brand-gradient text-white shadow-glow">
            <PiggyBank aria-hidden className="size-11" />
          </span>
        )}
        <div className="min-w-0">
          <p className="text-[clamp(1.25rem,9cqi,1.875rem)] font-extrabold tracking-tight break-words text-foreground">{formatMoney(savings.balance, savings.currency)}</p>
          {target !== null ? (
            <>
              <p className="text-sm text-muted-foreground">{formatMoney(target, savings.currency)} célból</p>
              {remaining > 0 ? (
                <p className="mt-1 text-sm font-semibold text-foreground">Még {formatMoney(remaining, savings.currency)} hiányzik</p>
              ) : (
                <p className="mt-1 flex items-center gap-1.5 text-sm font-semibold text-emerald-700 dark:text-emerald-400">
                  <PartyPopper aria-hidden className="size-4" />
                  Elérted a célt!
                </p>
              )}
            </>
          ) : (
            <p className="text-sm text-muted-foreground">Cél nélküli gyűjtés</p>
          )}
        </div>
      </div>

      <p className="mt-5 rounded-2xl bg-muted/70 px-4 py-3 text-sm text-muted-foreground">
        Következő havi kamat kb. <span className="font-semibold text-foreground">{formatMoney(interest, savings.currency)}</span>
      </p>

      <div className="mt-auto flex gap-2 pt-5">
        <Button size="sm" leftIcon={<ArrowDownLeft />} className="flex-1" disabled={!active} onClick={() => onAction('deposit')}>
          Befizetés
        </Button>
        <Button size="sm" variant="outline" leftIcon={<ArrowUpRight />} className="flex-1" disabled={!active || savings.balance <= 0} onClick={() => onAction('withdraw')}>
          Kivét
        </Button>
      </div>
    </div>
  )
}
