import { Archive, CirclePlay, Landmark, OctagonX, Snowflake, type LucideIcon } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse, AccountStatus } from '../../../api/types'
import Badge from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import { cn } from '../../../lib/cn'
import { accountStatusLabels, formatMoney, formatShortIban } from '../../../lib/format'
import { accountStatusTones } from '../accountStatus'
import { useUpdateAccountStatus } from '../queries'

const options: { status: AccountStatus; label: string; icon: LucideIcon; description: string }[] = [
  { status: 'ACTIVE', label: 'Aktív', icon: CirclePlay, description: 'A számla újra szabadon használható.' },
  { status: 'FROZEN', label: 'Befagyasztva', icon: Snowflake, description: 'Ideiglenesen nem indítható róla és nem érkezhet rá pénz.' },
  { status: 'BLOCKED', label: 'Letiltva', icon: OctagonX, description: 'Visszaélés gyanújakor: minden művelet tiltott rajta.' },
  { status: 'CLOSED', label: 'Lezárva', icon: Archive, description: 'Végleges: a kártyája megszűnik, a rendszeres átutalásai leállnak.' },
]

// Számlaállapot módosítása: a lezárás végleges, és csak nulla egyenlegnél lehetséges
export default function AccountStatusDialog({ account, onClose }: { account: AccountResponse | undefined; onClose: () => void }) {
  const updateStatus = useUpdateAccountStatus()

  // A kiúszó animáció alatt is látszik a tartalom; új számlánál a választás törlődik
  const [shown, setShown] = useState(account)
  const [target, setTarget] = useState<AccountStatus>()
  if (account && account !== shown) {
    setShown(account)
    setTarget(undefined)
  }

  function save() {
    if (!shown || !target) {
      return
    }
    updateStatus.mutate(
      { id: shown.id, status: target },
      {
        onSuccess: (updated) => {
          toast.success(`${updated.name}: ${accountStatusLabels[updated.status].toLowerCase()}`, { description: 'A tulajdonos értesítést kapott.' })
          onClose()
        },
        onError: (error) => toast.error(getErrorMessage(error)),
      },
    )
  }

  const closing = target === 'CLOSED'

  return (
    <Dialog
      open={account !== undefined}
      onClose={onClose}
      icon={<Landmark />}
      title="Számla állapota"
      description={shown ? `${shown.name} · ${formatShortIban(shown.accountNumber)}` : undefined}
    >
      {shown && (
        <>
          <div className="flex items-center justify-between gap-3 rounded-2xl bg-muted/70 px-4 py-3">
            <span className="text-sm text-muted-foreground">
              Egyenleg: <strong className="font-bold text-foreground tabular-nums">{formatMoney(shown.balance, shown.currency)}</strong>
            </span>
            <Badge tone={accountStatusTones[shown.status]}>{accountStatusLabels[shown.status]}</Badge>
          </div>

          <fieldset className="mt-5">
            <legend className="mb-3 text-sm font-semibold text-foreground">Új állapot</legend>
            <div className="space-y-2.5">
              {options
                .filter((option) => option.status !== shown.status)
                .map((option) => {
                  const disabled = option.status === 'CLOSED' && shown.balance !== 0
                  const danger = option.status === 'CLOSED' || option.status === 'BLOCKED'
                  return (
                    <label key={option.status} className={cn('block', disabled ? 'cursor-not-allowed' : 'cursor-pointer')}>
                      <input
                        type="radio"
                        name="account-status"
                        value={option.status}
                        checked={target === option.status}
                        disabled={disabled}
                        onChange={() => setTarget(option.status)}
                        className="peer sr-only"
                      />
                      <span
                        className={cn(
                          'flex items-start gap-3 rounded-2xl border border-border p-4 transition duration-200',
                          'peer-focus-visible:ring-4 peer-focus-visible:ring-ring/40 peer-disabled:opacity-50',
                          !disabled && 'hover:border-brand-300',
                          danger
                            ? 'peer-checked:border-danger/60 peer-checked:bg-danger/8'
                            : 'peer-checked:border-brand-400 peer-checked:bg-brand-50 dark:peer-checked:border-brand-500/60 dark:peer-checked:bg-brand-950/60',
                        )}
                      >
                        <span
                          className={cn(
                            'flex size-10 shrink-0 items-center justify-center rounded-xl [&_svg]:size-5',
                            danger ? 'bg-danger/12 text-danger' : 'bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300',
                          )}
                        >
                          <option.icon aria-hidden />
                        </span>
                        <span className="min-w-0">
                          <span className="block font-semibold text-foreground">{option.label}</span>
                          <span className="block text-sm text-muted-foreground">
                            {disabled ? 'Csak nulla egyenlegű számla zárható le.' : option.description}
                          </span>
                        </span>
                      </span>
                    </label>
                  )
                })}
            </div>
          </fieldset>
        </>
      )}

      <DialogActions>
        <Button variant="ghost" onClick={onClose} disabled={updateStatus.isPending}>
          Mégse
        </Button>
        <Button variant={closing ? 'danger' : 'primary'} disabled={!target} loading={updateStatus.isPending} onClick={save}>
          {closing ? 'Végleges lezárás' : 'Mentés'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
