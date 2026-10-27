import { ShoppingBag } from 'lucide-react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMemo, useState } from 'react'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { CardResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import AmountField from '../../../components/ui/AmountField'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import Select from '../../../components/ui/Select'
import Switch from '../../../components/ui/Switch'
import TextField from '../../../components/ui/TextField'
import { formatMaskedCard, formatMoney, formatSignedMoney, parseAmount } from '../../../lib/format'
import { newIdempotencyKey } from '../../../lib/idempotency'
import { useCategories } from '../../transactions/queries'
import { useCardPayment } from '../queries'
import { cardPaymentSchema, paymentPresets, type CardPaymentForm } from '../schemas'

interface CardPaymentDialogProps {
  card: CardResponse
  currency: string
  open: boolean
  onClose: () => void
}

// Demó kártyás fizetés; a limitet, a fedezetet és a kártya állapotát a backend ellenőrzi
export default function CardPaymentDialog({ card, currency, open, onClose }: CardPaymentDialogProps) {
  const payment = useCardPayment()
  const { data: categories = [] } = useCategories()
  const schema = useMemo(() => cardPaymentSchema(currency), [currency])
  const remaining = Math.max(card.dailyLimit - card.spentToday, 0)
  // Ablakmegnyitásonként egy kulcs: újrapróbáláskor a szerver nem terhel kétszer
  const [idempotencyKey, setIdempotencyKey] = useState(newIdempotencyKey)

  const {
    register,
    control,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<CardPaymentForm>({
    resolver: zodResolver(schema),
    defaultValues: { merchantName: '', amount: '', categoryId: '', online: false },
  })

  const online = useWatch({ control, name: 'online' })

  function close() {
    onClose()
    reset()
    payment.reset()
    setIdempotencyKey(newIdempotencyKey())
  }

  function applyPreset(preset: (typeof paymentPresets)[number]) {
    const category = categories.find((item) => item.name === preset.categoryName)
    setValue('merchantName', preset.merchantName, { shouldValidate: true })
    setValue('categoryId', category ? String(category.id) : '')
    setValue('online', preset.online)
  }

  function onSubmit(values: CardPaymentForm) {
    const amount = parseAmount(values.amount) ?? 0
    payment.mutate(
      {
        id: card.id,
        idempotencyKey,
        request: {
          merchantName: values.merchantName,
          amount,
          categoryId: values.categoryId ? Number(values.categoryId) : undefined,
          online: values.online,
        },
      },
      {
        onSuccess: () => {
          close()
          toast.success(`Sikeres fizetés: ${formatSignedMoney(-amount, currency)}`, { description: values.merchantName })
        },
      },
    )
  }

  return (
    <Dialog icon={<ShoppingBag />}
      open={open}
      onClose={close}
      title="Demó fizetés"
      description={`Kártyás vásárlás kipróbálása a(z) ${formatMaskedCard(card.maskedNumber)} kártyával.`}
    >
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div>
          <p className="mb-2 text-sm font-medium text-foreground">Gyors kitöltés</p>
          <div className="flex flex-wrap gap-2">
            {paymentPresets.map((preset) => (
              <button
                key={preset.merchantName}
                type="button"
                onClick={() => applyPreset(preset)}
                className="rounded-full border border-border bg-card px-3 py-1 text-sm font-semibold text-muted-foreground transition hover:border-brand-300 hover:text-foreground focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
              >
                {preset.merchantName}
              </button>
            ))}
          </div>
        </div>

        <TextField label="Kereskedő" placeholder="pl. Tesco" maxLength={150} error={errors.merchantName?.message} {...register('merchantName')} />

        <AmountField
          label="Összeg"
          currency={currency}
          placeholder={currency === 'HUF' ? '8 990' : '39,99'}
          hint={`Ma még ${formatMoney(remaining, currency)} költhető ezzel a kártyával.`}
          error={errors.amount?.message}
          {...register('amount')}
        />

        <Select label="Kategória (nem kötelező)" {...register('categoryId')}>
          <option value="">Nincs megadva</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </Select>

        <Controller
          control={control}
          name="online"
          render={({ field }) => (
            <Switch
              label="Online vásárlás"
              description={
                online && !card.onlinePaymentEnabled
                  ? 'Ezen a kártyán az online fizetés ki van kapcsolva, a fizetés sikertelen lesz.'
                  : 'Webáruházban, alkalmazásban történő fizetés.'
              }
              checked={field.value}
              onCheckedChange={field.onChange}
            />
          )}
        />

        {payment.isError && <Alert tone="danger">{getErrorMessage(payment.error)}</Alert>}

        <DialogActions>
          <Button variant="ghost" onClick={close}>
            Mégse
          </Button>
          <Button type="submit" loading={payment.isPending}>
            Fizetés
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
