import { PiggyBank, Plus } from 'lucide-react'
import { motion } from 'motion/react'
import { useState } from 'react'
import { getErrorMessage } from '../../../api/client'
import type { SavingsAccountResponse } from '../../../api/types'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import { cardSurface } from '../../../components/ui/cardStyles'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { cn } from '../../../lib/cn'
import { currencyName, formatMoney } from '../../../lib/format'
import { staggerContainer, staggerItem } from '../../../lib/motion'
import { useAccounts } from '../../accounts/queries'
import CloseSavingsDialog from '../components/CloseSavingsDialog'
import CreateSavingsDialog from '../components/CreateSavingsDialog'
import EditSavingsDialog from '../components/EditSavingsDialog'
import SavingsGoalCard, { type SavingsAction } from '../components/SavingsGoalCard'
import SavingsHistoryDialog from '../components/SavingsHistoryDialog'
import SavingsSkeleton from '../components/SavingsSkeleton'
import SavingsTransferDialog from '../components/SavingsTransferDialog'
import { useSavings } from '../queries'
import { estimatedMonthlyInterest } from '../schemas'

// Melyik ablak van (vagy volt utoljára) nyitva, melyik megtakarításhoz. Bezáráskor csak az "open" vált hamisra, így az
// ablak kiúszó animációja lefut; a kulcs minden megnyitáskor nő, így az űrlap mindig friss értékekkel indul.
type DialogTarget = { kind: 'create' } | { kind: SavingsAction; savings: SavingsAccountResponse }
type DialogState = DialogTarget & { open: boolean }

// Pénznemenként: összesen megtakarítva és a következő havi kamat becslése
function totalsByCurrency(savings: SavingsAccountResponse[]) {
  const totals = new Map<string, { balance: number; interest: number; count: number }>()
  for (const item of savings) {
    const current = totals.get(item.currency) ?? { balance: 0, interest: 0, count: 0 }
    totals.set(item.currency, {
      balance: current.balance + item.balance,
      interest: current.interest + estimatedMonthlyInterest(item.balance, item.interestRate),
      count: current.count + 1,
    })
  }
  return [...totals.entries()]
}

// Megtakarítások: a célok kártyákon (haladás, egyenleg, kamat), felettük az összesítés; a műveletek ablakokban
export default function SavingsPage() {
  const savingsQuery = useSavings()
  const { data: accounts = [] } = useAccounts()
  const [dialog, setDialog] = useState<DialogState | null>(null)
  const [dialogKey, setDialogKey] = useState(0)

  const savings = savingsQuery.data ?? []
  const hasActiveAccount = accounts.some((account) => account.status === 'ACTIVE')

  function open(next: DialogTarget) {
    setDialog({ ...next, open: true })
    setDialogKey((key) => key + 1)
  }

  const close = () => setDialog((current) => current && { ...current, open: false })

  return (
    <>
      <PageHeader
        title="Megtakarítások"
        description="Célok, amelyekre gyűjtesz – havi kamattal."
        actions={
          <Button variant="glass" leftIcon={<Plus />} disabled={!hasActiveAccount} onClick={() => open({ kind: 'create' })}>
            Új megtakarítás
          </Button>
        }
      />

      {savingsQuery.isPending ? (
        <SavingsSkeleton />
      ) : savingsQuery.isError ? (
        <Card>
          <QueryError message={getErrorMessage(savingsQuery.error)} retrying={savingsQuery.isFetching} onRetry={() => void savingsQuery.refetch()} />
        </Card>
      ) : savings.length === 0 ? (
        <Card>
          <EmptyState
            icon={<PiggyBank />}
            title="Még nincs megtakarításod"
            description="Nyaralás, új telefon vagy vésztartalék: adj nevet a célodnak, és havonta kamatot írunk jóvá rá."
            action={
              <Button leftIcon={<Plus />} disabled={!hasActiveAccount} onClick={() => open({ kind: 'create' })}>
                Megtakarítás indítása
              </Button>
            }
          />
        </Card>
      ) : (
        <>
          <motion.ul variants={staggerContainer} initial="hidden" animate="show" className="grid grid-cols-1 gap-6 md:grid-cols-2 2xl:grid-cols-3">
            {savings.map((item) => (
              <motion.li key={item.id} variants={staggerItem} className={cn(cardSurface, 'spotlight')}>
                <SavingsGoalCard savings={item} onAction={(kind) => open({ kind, savings: item })} />
              </motion.li>
            ))}
          </motion.ul>

          <Card className="mt-6 p-6">
            <CardHeader title="Összesítés" description="A következő havi kamat a mostani egyenlegből becsülve (egyenleg × éves kamatláb / 12)." />
            <dl className="mt-5 grid grid-cols-[repeat(auto-fit,minmax(min(14rem,100%),1fr))] gap-3">
              {totalsByCurrency(savings).map(([currency, total]) => (
                <div key={currency} className="rounded-2xl bg-muted/70 p-4">
                  <dt className="text-sm font-medium text-muted-foreground">
                    {currencyName(currency)} · {total.count} cél
                  </dt>
                  <dd className="mt-1 text-2xl font-extrabold tracking-tight text-foreground">{formatMoney(total.balance, currency)}</dd>
                  <dd className="mt-0.5 text-sm text-muted-foreground">+{formatMoney(total.interest, currency)} kamat a következő hónapban</dd>
                </div>
              ))}
            </dl>
          </Card>
        </>
      )}

      {dialog?.kind === 'create' && <CreateSavingsDialog key={dialogKey} open={dialog.open} onClose={close} accounts={accounts} />}
      {(dialog?.kind === 'deposit' || dialog?.kind === 'withdraw') && (
        <SavingsTransferDialog key={dialogKey} open={dialog.open} savings={dialog.savings} direction={dialog.kind} accounts={accounts} onClose={close} />
      )}
      {dialog?.kind === 'edit' && <EditSavingsDialog key={dialogKey} open={dialog.open} savings={dialog.savings} onClose={close} />}
      {dialog?.kind === 'close' && <CloseSavingsDialog key={dialogKey} open={dialog.open} savings={dialog.savings} accounts={accounts} onClose={close} />}
      {dialog?.kind === 'history' && <SavingsHistoryDialog key={dialogKey} open={dialog.open} savings={dialog.savings} onClose={close} />}
    </>
  )
}
