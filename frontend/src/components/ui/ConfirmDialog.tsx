import { CircleHelp, TriangleAlert } from 'lucide-react'
import type { ReactNode } from 'react'
import Button from './Button'
import Dialog, { DialogActions } from './Dialog'

interface ConfirmDialogProps {
  open: boolean
  onClose: () => void
  onConfirm: () => void
  title: string
  description?: string
  children?: ReactNode
  confirmLabel: string
  loading?: boolean
  tone?: 'danger' | 'primary'
}

// Megerősítés egy nehezen visszafordítható művelet előtt (pl. számla lezárása): figyelmeztető ikon, a következmény
// leírása, alul a "Mégse" és a (veszélyes műveletnél piros) megerősítő gomb
export default function ConfirmDialog({
  open,
  onClose,
  onConfirm,
  title,
  description,
  children,
  confirmLabel,
  loading = false,
  tone = 'danger',
}: ConfirmDialogProps) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={title}
      description={description}
      icon={tone === 'danger' ? <TriangleAlert /> : <CircleHelp />}
      tone={tone === 'danger' ? 'danger' : 'brand'}
    >
      {children}
      <DialogActions>
        <Button variant="ghost" onClick={onClose} disabled={loading}>
          Mégse
        </Button>
        <Button variant={tone} loading={loading} onClick={onConfirm}>
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
