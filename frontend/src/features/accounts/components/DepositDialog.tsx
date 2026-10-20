import { Banknote } from 'lucide-react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import AmountField from '../../../components/ui/AmountField'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import TextField from '../../../components/ui/TextField'
import { formatMoney, parseAmount } from '../../../lib/format'
import { useDeposit } from '../queries'
import { depositSchema, quickAmounts, type DepositForm } from '../schemas'

const amountInputFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 2 })

interface DepositDialogProps {
  account: AccountResponse
  open: boolean
  onClose: () => void
}

// Demó készpénzbefizetés egy számlára (így a próbafelhasználók is könnyen tudnak "pénzt tenni" a számlájukra)
export default function DepositDialog({ account, open, onClose }: DepositDialogProps) {
  const depositMutation = useDeposit()
  const schema = useMemo(() => depositSchema(account.currency), [account.currency])

  const {
    register,
    handleSubmit,
    setValue,
    reset,
    formState: { errors },
  } = useForm<DepositForm>({ resolver: zodResolver(schema), defaultValues: { amount: '', message: '' } })

  function close() {
    onClose()
    reset()
    depositMutation.reset()
  }

  function onSubmit(values: DepositForm) {
    const amount = parseAmount(values.amount) ?? 0
    depositMutation.mutate(
      { accountId: account.id, request: { amount, message: values.message || undefined } },
      {
        onSuccess: () => {
          close()
          toast.success(`${formatMoney(amount, account.currency)} jóváírva`, { description: `${account.name} – készpénzbefizetés` })
        },
      },
    )
  }

  return (
    <Dialog icon={<Banknote />} open={open} onClose={close} title="Befizetés" description={`Demó készpénzbefizetés a(z) „${account.name}” számlára.`}>
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div>
          <AmountField label="Összeg" currency={account.currency} placeholder={account.currency === 'HUF' ? '25 000' : '100'} error={errors.amount?.message} {...register('amount')} />
          <div className="mt-3 flex flex-wrap gap-2">
            {(quickAmounts[account.currency] ?? []).map((amount) => (
              <button
                key={amount}
                type="button"
                onClick={() => setValue('amount', amountInputFormatter.format(amount), { shouldValidate: true, shouldDirty: true })}
                className="rounded-full border border-border bg-card px-3 py-1 text-sm font-semibold text-muted-foreground transition hover:border-brand-300 hover:text-foreground focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
              >
                {formatMoney(amount, account.currency)}
              </button>
            ))}
          </div>
        </div>

        <TextField label="Közlemény (nem kötelező)" placeholder="Készpénzbefizetés" maxLength={255} error={errors.message?.message} {...register('message')} />

        {depositMutation.isError && <Alert tone="danger">{getErrorMessage(depositMutation.error)}</Alert>}

        <DialogActions>
          <Button variant="ghost" onClick={close}>
            Mégse
          </Button>
          <Button type="submit" loading={depositMutation.isPending}>
            Befizetés
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
