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
import { formatMoney, formatShortIban, parseAmount } from '../../../lib/format'
import { depositSchema, quickAmounts, type DepositForm } from '../../accounts/schemas'
import { useAdminDeposit } from '../queries'

const amountInputFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 2 })

interface AdminDepositDialogProps {
  account: AccountResponse | undefined
  onClose: () => void
}

// Készpénzbefizetés rögzítése ügyfélszámlára: csak admin teheti, az audit naplóba kerül
export default function AdminDepositDialog({ account, onClose }: AdminDepositDialogProps) {
  const depositMutation = useAdminDeposit()
  const currency = account?.currency ?? 'HUF'
  const schema = useMemo(() => depositSchema(currency), [currency])

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
    if (!account) {
      return
    }
    const amount = parseAmount(values.amount) ?? 0
    depositMutation.mutate(
      { accountId: account.id, request: { amount, message: values.message || undefined } },
      {
        onSuccess: () => {
          close()
          toast.success(`${formatMoney(amount, account.currency)} jóváírva`, { description: `${account.name} – az ügyfél értesítést kapott` })
        },
      },
    )
  }

  return (
    <Dialog
      icon={<Banknote />}
      open={account !== undefined}
      onClose={close}
      title="Készpénzbefizetés rögzítése"
      description={account ? `${account.name} · ${formatShortIban(account.accountNumber)}` : undefined}
    >
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div>
          <AmountField label="Összeg" currency={currency} placeholder={currency === 'HUF' ? '25 000' : '100'} error={errors.amount?.message} {...register('amount')} />
          <div className="mt-3 flex flex-wrap gap-2">
            {(quickAmounts[currency] ?? []).map((amount) => (
              <button
                key={amount}
                type="button"
                onClick={() => setValue('amount', amountInputFormatter.format(amount), { shouldValidate: true, shouldDirty: true })}
                className="rounded-full border border-border bg-card px-3 py-1 text-sm font-semibold text-muted-foreground transition hover:border-brand-300 hover:text-foreground focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
              >
                {formatMoney(amount, currency)}
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
            Jóváírás
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
