import { Archive } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import ConfirmDialog from '../../../components/ui/ConfirmDialog'
import { useCloseAccount } from '../queries'

// Backend szabály: csak nulla egyenlegű, nem utolsó nyitott számla zárható le
function closeBlocker(account: AccountResponse, openAccountCount: number): string | null {
  if (account.balance !== 0) {
    return 'Csak nulla egyenlegű számla zárható le. Előbb utald át a pénzt egy másik számládra.'
  }
  if (openAccountCount <= 1) {
    return 'Az utolsó nyitott számládat nem zárhatod le.'
  }
  return null
}

export default function CloseAccountCard({ account, openAccountCount }: { account: AccountResponse; openAccountCount: number }) {
  const navigate = useNavigate()
  const closeMutation = useCloseAccount()
  const [confirming, setConfirming] = useState(false)
  const blocker = closeBlocker(account, openAccountCount)

  function confirmClose() {
    closeMutation.mutate(account.id, {
      onSuccess: () => {
        setConfirming(false)
        toast.success('A számlát lezártuk', { description: `${account.name} – a hozzá tartozó bankkártya is megszűnt.` })
        navigate('/accounts')
      },
    })
  }

  return (
    <Card className="flex flex-col gap-4 p-6 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex items-start gap-4">
        <span className="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-danger/10 text-danger">
          <Archive className="size-5" />
        </span>
        <div>
          <h2 className="font-bold text-foreground">Számla lezárása</h2>
          <p className="mt-0.5 max-w-xl text-sm text-muted-foreground">{blocker ?? 'A lezárt számla nem használható tovább, a hozzá tartozó bankkártya is megszűnik.'}</p>
        </div>
      </div>
      <Button variant="outline" className="shrink-0 hover:border-danger/60 hover:text-danger" disabled={blocker !== null} onClick={() => setConfirming(true)}>
        Lezárás
      </Button>

      <ConfirmDialog
        open={confirming}
        onClose={() => {
          setConfirming(false)
          closeMutation.reset()
        }}
        onConfirm={confirmClose}
        loading={closeMutation.isPending}
        title="Biztosan lezárod a számlát?"
        description={`A(z) „${account.name}” számla és a hozzá tartozó bankkártya megszűnik. A művelet nem vonható vissza.`}
        confirmLabel="Számla lezárása"
      >
        {closeMutation.isError && <Alert tone="danger">{getErrorMessage(closeMutation.error)}</Alert>}
      </ConfirmDialog>
    </Card>
  )
}
