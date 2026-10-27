import { CalendarClock } from 'lucide-react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useCallback, useState } from 'react'
import { Controller, useForm, useWatch, type Resolver } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse, BeneficiaryResponse, StandingOrderResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import AmountField from '../../../components/ui/AmountField'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import IbanField from '../../../components/ui/IbanField'
import SegmentedControl from '../../../components/ui/SegmentedControl'
import Select from '../../../components/ui/Select'
import TextField from '../../../components/ui/TextField'
import { toIsoDate } from '../../../lib/dates'
import { formatIban, formatMoney, frequencyLabels, parseAmount } from '../../../lib/format'
import { isValidIban, normalizeIban } from '../../../lib/iban'
import { useCreateStandingOrder, useUpdateStandingOrder } from '../queries'
import { standingOrderSchema, type StandingOrderForm } from '../schemas'

const amountInputFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 2 })

interface StandingOrderDialogProps {
  open: boolean
  onClose: () => void
  // Megadva módosítás (a számla és a címzett nem változhat), egyébként új megbízás
  order?: StandingOrderResponse
  accounts: AccountResponse[]
  beneficiaries: BeneficiaryResponse[]
}

// Rendszeres átutalás létrehozása vagy módosítása (a szülő megnyitáskor újrakulcsolja)
export default function StandingOrderDialog({ open, onClose, order, accounts, beneficiaries }: StandingOrderDialogProps) {
  const [today] = useState(() => toIsoDate(new Date()))
  const createOrder = useCreateStandingOrder()
  const updateOrder = useUpdateStandingOrder()
  const mutation = order ? updateOrder : createOrder
  const activeAccounts = accounts.filter((account) => account.status === 'ACTIVE')

  // Az összeg szabálya a kiválasztott számla pénznemétől függ
  const resolver = useCallback<Resolver<StandingOrderForm>>(
    (values, context, options) => {
      const currency = accounts.find((account) => String(account.id) === values.accountId)?.currency ?? 'HUF'
      return zodResolver(standingOrderSchema(currency, today))(values, context, options)
    },
    [accounts, today],
  )

  const {
    register,
    control,
    handleSubmit,
    setValue,
    reset,
    formState: { errors },
  } = useForm<StandingOrderForm>({
    resolver,
    defaultValues: order
      ? {
          accountId: String(order.accountId),
          toAccountNumber: order.toAccountNumber,
          partnerName: order.partnerName,
          amount: amountInputFormatter.format(order.amount),
          message: order.message ?? '',
          frequency: order.frequency,
          date: order.nextExecutionDate,
        }
      : {
          accountId: activeAccounts[0] ? String(activeAccounts[0].id) : '',
          toAccountNumber: '',
          partnerName: '',
          amount: '',
          message: '',
          frequency: 'MONTHLY',
          date: today,
        },
  })

  const accountId = useWatch({ control, name: 'accountId' })
  const toAccountNumber = useWatch({ control, name: 'toAccountNumber' })
  const currency = accounts.find((account) => String(account.id) === accountId)?.currency ?? 'HUF'

  function close() {
    onClose()
    reset()
    createOrder.reset()
    updateOrder.reset()
  }

  function fillFromBeneficiary(id: string) {
    const beneficiary = beneficiaries.find((item) => String(item.id) === id)
    if (beneficiary) {
      setValue('toAccountNumber', formatIban(beneficiary.accountNumber), { shouldValidate: true })
      setValue('partnerName', beneficiary.name, { shouldValidate: true })
    }
  }

  function onSubmit(values: StandingOrderForm) {
    const amount = parseAmount(values.amount) ?? 0
    const onSuccess = () => {
      close()
      toast.success(order ? 'Mentettük a módosítást' : 'Rendszeres átutalás beállítva', {
        description: `${values.partnerName} – ${formatMoney(amount, currency)}, ${frequencyLabels[values.frequency].toLowerCase()}`,
      })
    }

    if (order) {
      updateOrder.mutate(
        { id: order.id, request: { amount, message: values.message || undefined, frequency: values.frequency, nextExecutionDate: values.date } },
        { onSuccess },
      )
    } else {
      createOrder.mutate(
        {
          accountId: Number(values.accountId),
          toAccountNumber: normalizeIban(values.toAccountNumber),
          partnerName: values.partnerName,
          amount,
          message: values.message || undefined,
          frequency: values.frequency,
          firstExecutionDate: values.date,
        },
        { onSuccess },
      )
    }
  }

  const sourceAccount = accounts.find((account) => String(account.id) === accountId)

  return (
    <Dialog icon={<CalendarClock />}
      open={open}
      onClose={close}
      title={order ? 'Megbízás módosítása' : 'Új rendszeres átutalás'}
      description={order ? undefined : 'Hetente vagy havonta, a megadott naptól kezdve magától elküldjük.'}
    >
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        {order ? (
          <dl className="space-y-2 rounded-2xl bg-muted/70 p-4 text-sm">
            <div>
              <dt className="text-muted-foreground">Kinek</dt>
              <dd className="font-semibold text-foreground">
                {order.partnerName} · <span className="font-mono">{formatIban(order.toAccountNumber)}</span>
              </dd>
            </div>
            <div>
              <dt className="text-muted-foreground">Honnan</dt>
              <dd className="font-semibold text-foreground">{sourceAccount?.name ?? formatIban(order.accountNumber)}</dd>
            </div>
          </dl>
        ) : (
          <>
            <Select label="Honnan" error={errors.accountId?.message} {...register('accountId')}>
              {activeAccounts.map((account) => (
                <option key={account.id} value={account.id}>
                  {account.name} ({formatMoney(account.balance, account.currency)})
                </option>
              ))}
            </Select>
            {beneficiaries.length > 0 && (
              <Select label="Kedvezményezett (gyors kitöltés)" value="" onChange={(event) => fillFromBeneficiary(event.target.value)}>
                <option value="">Válassz a mentett címzettek közül…</option>
                {beneficiaries.map((beneficiary) => (
                  <option key={beneficiary.id} value={beneficiary.id}>
                    {beneficiary.name}
                  </option>
                ))}
              </Select>
            )}
            <IbanField label="A címzett számlaszáma (IBAN)" valid={isValidIban(toAccountNumber ?? '')} error={errors.toAccountNumber?.message} {...register('toAccountNumber')} />
            <TextField label="A címzett neve" placeholder="pl. Tóth Gábor" maxLength={150} error={errors.partnerName?.message} {...register('partnerName')} />
          </>
        )}

        <AmountField label="Összeg" currency={currency} placeholder={currency === 'HUF' ? '220 000' : '100'} error={errors.amount?.message} {...register('amount')} />

        <div>
          <p className="mb-1.5 text-sm font-medium text-foreground">Gyakoriság</p>
          <Controller
            control={control}
            name="frequency"
            render={({ field }) => (
              <SegmentedControl
                label="Gyakoriság"
                value={field.value}
                onChange={field.onChange}
                options={[
                  { value: 'MONTHLY', label: frequencyLabels.MONTHLY },
                  { value: 'WEEKLY', label: frequencyLabels.WEEKLY },
                ]}
              />
            )}
          />
        </div>

        <TextField label={order ? 'Következő teljesítés' : 'Első teljesítés'} type="date" min={today} error={errors.date?.message} {...register('date')} />

        <TextField label="Közlemény (nem kötelező)" placeholder="pl. Albérlet" maxLength={255} error={errors.message?.message} {...register('message')} />

        {mutation.isError && <Alert tone="danger">{getErrorMessage(mutation.error)}</Alert>}

        <DialogActions>
          <Button variant="ghost" onClick={close}>
            Mégse
          </Button>
          <Button type="submit" loading={mutation.isPending}>
            {order ? 'Mentés' : 'Beállítás'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
