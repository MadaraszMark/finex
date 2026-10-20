import { zodResolver } from '@hookform/resolvers/zod'
import { PiggyBank } from 'lucide-react'
import { useCallback } from 'react'
import { useForm, useWatch, type Resolver } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import AmountField from '../../../components/ui/AmountField'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import Select from '../../../components/ui/Select'
import TextField from '../../../components/ui/TextField'
import { formatMoney, parseAmount } from '../../../lib/format'
import { useCreateSavings } from '../queries'
import { createSavingsSchema, type CreateSavingsForm } from '../schemas'

interface CreateSavingsDialogProps {
  open: boolean
  onClose: () => void
  accounts: AccountResponse[]
}

// Új megtakarítási cél: név, a folyószámla (ez adja a pénznemet és a kezdő összeget), opcionális célösszeg
export default function CreateSavingsDialog({ open, onClose, accounts }: CreateSavingsDialogProps) {
  const createSavings = useCreateSavings()
  const activeAccounts = accounts.filter((account) => account.status === 'ACTIVE')

  const resolver = useCallback<Resolver<CreateSavingsForm>>(
    (values, context, options) => {
      const currency = accounts.find((account) => String(account.id) === values.accountId)?.currency ?? 'HUF'
      return zodResolver(createSavingsSchema(currency))(values, context, options)
    },
    [accounts],
  )

  const {
    register,
    control,
    handleSubmit,
    setError,
    reset,
    formState: { errors },
  } = useForm<CreateSavingsForm>({
    resolver,
    defaultValues: { name: '', accountId: activeAccounts[0] ? String(activeAccounts[0].id) : '', initialDeposit: '', targetAmount: '' },
  })
  const accountId = useWatch({ control, name: 'accountId' })
  const account = accounts.find((item) => String(item.id) === accountId)
  const currency = account?.currency ?? 'HUF'

  function close() {
    onClose()
    reset()
    createSavings.reset()
  }

  function onSubmit(values: CreateSavingsForm) {
    const initialDeposit = parseAmount(values.initialDeposit) ?? 0
    if (account && initialDeposit > account.balance) {
      setError('initialDeposit', { message: `Nincs elég fedezet: a számla egyenlege ${formatMoney(account.balance, currency)}.` })
      return
    }
    createSavings.mutate(
      {
        name: values.name,
        accountId: Number(values.accountId),
        initialDeposit,
        targetAmount: parseAmount(values.targetAmount),
      },
      {
        onSuccess: (savings) => {
          close()
          toast.success('Elindult az új megtakarításod', {
            description: initialDeposit > 0 ? `${savings.name} – ${formatMoney(initialDeposit, currency)} az induláshoz` : savings.name,
          })
        },
      },
    )
  }

  return (
    <Dialog icon={<PiggyBank />} open={open} onClose={close} title="Új megtakarítás" description="Adj nevet a célodnak; havonta kamatot írunk jóvá rá.">
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <TextField label="Mire gyűjtesz?" placeholder="pl. Nyaralás, Új laptop" maxLength={100} error={errors.name?.message} {...register('name')} />

        <Select label="Folyószámla" hint="A megtakarítás pénzneme ennek a számlának a pénzneme lesz." error={errors.accountId?.message} {...register('accountId')}>
          {activeAccounts.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name} ({formatMoney(item.balance, item.currency)})
            </option>
          ))}
        </Select>

        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
          <AmountField
            label="Kezdő összeg (nem kötelező)"
            currency={currency}
            placeholder="0"
            error={errors.initialDeposit?.message}
            {...register('initialDeposit')}
          />
          <AmountField
            label="Célösszeg (nem kötelező)"
            currency={currency}
            placeholder={currency === 'HUF' ? '600 000' : '1 000'}
            error={errors.targetAmount?.message}
            {...register('targetAmount')}
          />
        </div>

        <p className="flex items-start gap-2.5 rounded-2xl bg-muted/70 px-4 py-3 text-sm text-muted-foreground">
          <PiggyBank aria-hidden className="mt-0.5 size-4 shrink-0 text-primary" />
          A pénzt bármikor visszaveheted a folyószámládra; lezáráskor a teljes egyenleg visszakerül.
        </p>

        {createSavings.isError && <Alert tone="danger">{getErrorMessage(createSavings.error)}</Alert>}

        <DialogActions>
          <Button variant="ghost" onClick={close}>
            Mégse
          </Button>
          <Button type="submit" loading={createSavings.isPending} disabled={activeAccounts.length === 0}>
            Megtakarítás indítása
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
