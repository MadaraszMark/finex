import { Pencil } from 'lucide-react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { SavingsAccountResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import AmountField from '../../../components/ui/AmountField'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import TextField from '../../../components/ui/TextField'
import { parseAmount } from '../../../lib/format'
import { useUpdateSavings } from '../queries'
import { updateSavingsSchema, type UpdateSavingsForm } from '../schemas'

const amountInputFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 2 })

// A megtakarítás neve és célösszege (üres célösszeggel cél nélküli gyűjtés lesz)
export default function EditSavingsDialog({ savings, open, onClose }: { savings: SavingsAccountResponse; open: boolean; onClose: () => void }) {
  const updateSavings = useUpdateSavings()
  const schema = useMemo(() => updateSavingsSchema(savings.currency), [savings.currency])

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<UpdateSavingsForm>({
    resolver: zodResolver(schema),
    defaultValues: { name: savings.name, targetAmount: savings.targetAmount !== null ? amountInputFormatter.format(savings.targetAmount) : '' },
  })

  function close() {
    onClose()
    reset()
    updateSavings.reset()
  }

  function onSubmit(values: UpdateSavingsForm) {
    updateSavings.mutate(
      { id: savings.id, request: { name: values.name, targetAmount: parseAmount(values.targetAmount) } },
      {
        onSuccess: () => {
          close()
          toast.success('Mentettük a módosítást', { description: values.name })
        },
      },
    )
  }

  return (
    <Dialog icon={<Pencil />} open={open} onClose={close} title="Megtakarítás szerkesztése">
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <TextField label="Mire gyűjtesz?" maxLength={100} error={errors.name?.message} {...register('name')} />
        <AmountField
          label="Célösszeg (nem kötelező)"
          currency={savings.currency}
          hint="Üresen hagyva cél nélküli gyűjtés lesz."
          error={errors.targetAmount?.message}
          {...register('targetAmount')}
        />

        {updateSavings.isError && <Alert tone="danger">{getErrorMessage(updateSavings.error)}</Alert>}

        <DialogActions>
          <Button variant="ghost" onClick={close}>
            Mégse
          </Button>
          <Button type="submit" loading={updateSavings.isPending}>
            Mentés
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
