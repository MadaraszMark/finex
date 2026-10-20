import { useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse, SavingsAccountResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import ConfirmDialog from '../../../components/ui/ConfirmDialog'
import Select from '../../../components/ui/Select'
import { formatMoney } from '../../../lib/format'
import { useCloseSavings } from '../queries'

interface CloseSavingsDialogProps {
  savings: SavingsAccountResponse
  accounts: AccountResponse[]
  open: boolean
  onClose: () => void
}

// Lezárás: a teljes egyenleg a kiválasztott (azonos pénznemű, aktív) folyószámlára kerül, a cél megszűnik
export default function CloseSavingsDialog({ savings, accounts, open, onClose }: CloseSavingsDialogProps) {
  const closeSavings = useCloseSavings()
  const eligible = accounts.filter((account) => account.status === 'ACTIVE' && account.currency === savings.currency)
  const [accountId, setAccountId] = useState(eligible[0] ? String(eligible[0].id) : '')

  function close() {
    onClose()
    closeSavings.reset()
  }

  function confirm() {
    closeSavings.mutate(
      { id: savings.id, accountId: Number(accountId) },
      {
        onSuccess: () => {
          close()
          toast.success('Lezártad a megtakarítást', {
            description: savings.balance > 0 ? `${formatMoney(savings.balance, savings.currency)} visszakerült a folyószámládra.` : savings.name,
          })
        },
      },
    )
  }

  return (
    <ConfirmDialog
      open={open}
      onClose={close}
      onConfirm={confirm}
      loading={closeSavings.isPending}
      title="Lezárod a megtakarítást?"
      description={
        savings.balance > 0
          ? `A(z) „${savings.name}” teljes egyenlege (${formatMoney(savings.balance, savings.currency)}) a kiválasztott számlára kerül, a cél megszűnik.`
          : `A(z) „${savings.name}” megtakarítás megszűnik.`
      }
      confirmLabel="Lezárás"
    >
      {eligible.length === 0 ? (
        <Alert tone="info">Nincs aktív, {savings.currency} pénznemű folyószámlád, amelyre a pénz visszakerülhetne.</Alert>
      ) : (
        <Select label="Melyik számlára kerüljön a pénz?" value={accountId} onChange={(event) => setAccountId(event.target.value)}>
          {eligible.map((account) => (
            <option key={account.id} value={account.id}>
              {account.name} ({formatMoney(account.balance, account.currency)})
            </option>
          ))}
        </Select>
      )}
      {closeSavings.isError && (
        <Alert tone="danger" className="mt-4">
          {getErrorMessage(closeSavings.error)}
        </Alert>
      )}
    </ConfirmDialog>
  )
}
