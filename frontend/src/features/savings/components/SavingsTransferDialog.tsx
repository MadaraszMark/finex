import { ArrowDownLeft, ArrowUpRight } from 'lucide-react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMemo } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse, SavingsAccountResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import AmountField from '../../../components/ui/AmountField'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import Select from '../../../components/ui/Select'
import TextField from '../../../components/ui/TextField'
import { formatMoney, parseAmount } from '../../../lib/format'
import { useSavingsTransfer } from '../queries'
import { savingsTransferSchema, type SavingsTransferForm } from '../schemas'

const amountInputFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 2 })

interface SavingsTransferDialogProps {
  savings: SavingsAccountResponse
  direction: 'deposit' | 'withdraw'
  accounts: AccountResponse[]
  open: boolean
  onClose: () => void
}

// Befizetés vagy kivét (csak azonos pénznemű, aktív folyószámlával)
export default function SavingsTransferDialog({ savings, direction, accounts, open, onClose }: SavingsTransferDialogProps) {
  const transfer = useSavingsTransfer()
  const deposit = direction === 'deposit'
  const eligible = accounts.filter((account) => account.status === 'ACTIVE' && account.currency === savings.currency)
  const schema = useMemo(() => savingsTransferSchema(savings.currency), [savings.currency])

  const {
    register,
    control,
    handleSubmit,
    setValue,
    setError,
    reset,
    formState: { errors },
  } = useForm<SavingsTransferForm>({
    resolver: zodResolver(schema),
    defaultValues: { accountId: eligible[0] ? String(eligible[0].id) : '', amount: '', message: '' },
  })
  const accountId = useWatch({ control, name: 'accountId' })
  const account = eligible.find((item) => String(item.id) === accountId)
  // Befizetésnél a folyószámla egyenlege, kivétnél a megtakarításé a felső határ
  const available = deposit ? (account?.balance ?? 0) : savings.balance

  function close() {
    onClose()
    reset()
    transfer.reset()
  }

  function onSubmit(values: SavingsTransferForm) {
    const amount = parseAmount(values.amount) ?? 0
    if (amount > available) {
      setError('amount', { message: `Legfeljebb ${formatMoney(available, savings.currency)} ${deposit ? 'tehető be' : 'vehető ki'}.` })
      return
    }
    transfer.mutate(
      { id: savings.id, direction, request: { accountId: Number(values.accountId), amount, message: values.message || undefined } },
      {
        onSuccess: (response) => {
          close()
          toast.success(deposit ? `${formatMoney(amount, savings.currency)} betéve` : `${formatMoney(amount, savings.currency)} kivéve`, {
            description: `${savings.name} új egyenlege: ${formatMoney(response.savingsNewBalance, savings.currency)}`,
          })
        },
      },
    )
  }

  return (
    <Dialog icon={direction === 'deposit' ? <ArrowDownLeft /> : <ArrowUpRight />}
      open={open}
      onClose={close}
      title={deposit ? 'Befizetés a megtakarításra' : 'Kivét a megtakarításból'}
      description={`${savings.name} · egyenleg: ${formatMoney(savings.balance, savings.currency)}`}
    >
      {eligible.length === 0 ? (
        <Alert tone="info">Nincs aktív, {savings.currency} pénznemű folyószámlád, ezért most nem mozgatható pénz.</Alert>
      ) : (
        <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <Select label={deposit ? 'Honnan' : 'Hová'} error={errors.accountId?.message} {...register('accountId')}>
            {eligible.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name} ({formatMoney(item.balance, item.currency)})
              </option>
            ))}
          </Select>

          <div>
            <AmountField
              label="Összeg"
              currency={savings.currency}
              placeholder={savings.currency === 'HUF' ? '50 000' : '100'}
              hint={`Legfeljebb ${formatMoney(available, savings.currency)}`}
              error={errors.amount?.message}
              {...register('amount')}
            />
            {available > 0 && (
              <button
                type="button"
                onClick={() => setValue('amount', amountInputFormatter.format(available), { shouldValidate: true })}
                className="mt-2 rounded-full border border-border bg-card px-3 py-1 text-sm font-semibold text-muted-foreground transition hover:border-brand-300 hover:text-foreground focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
              >
                {deposit ? 'A teljes egyenleg' : 'Az egész összeg'}
              </button>
            )}
          </div>

          <TextField label="Közlemény (nem kötelező)" maxLength={255} error={errors.message?.message} {...register('message')} />

          {transfer.isError && <Alert tone="danger">{getErrorMessage(transfer.error)}</Alert>}

          <DialogActions>
            <Button variant="ghost" onClick={close}>
              Mégse
            </Button>
            <Button type="submit" loading={transfer.isPending}>
              {deposit ? 'Befizetés' : 'Kivét'}
            </Button>
          </DialogActions>
        </form>
      )}
    </Dialog>
  )
}
