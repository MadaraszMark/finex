import { useState, type FormEvent } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { CardResponse } from '../../../api/types'
import AmountField from '../../../components/ui/AmountField'
import Button from '../../../components/ui/Button'
import Slider from '../../../components/ui/Slider'
import { formatMoney, parseAmount } from '../../../lib/format'
import { useUpdateCardLimits } from '../queries'
import { MAX_DAILY_LIMIT, limitSliderBounds } from '../schemas'

const inputFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 2 })

interface LimitFormProps {
  card: CardResponse
  currency: string
  disabled?: boolean
}

// A napi költési limit: csúszkával gyorsan, a mezőben pontosan állítható, a Mentés gomb csak változáskor aktív.
// (A komponens kártyánként újraindul, a szülő a kártya azonosítójával kulcsolja.)
export default function LimitForm({ card, currency, disabled = false }: LimitFormProps) {
  const updateLimits = useUpdateCardLimits()
  const bounds = limitSliderBounds(currency)
  const [value, setValue] = useState(card.dailyLimit)
  const [input, setInput] = useState(inputFormatter.format(card.dailyLimit))

  const parsed = parseAmount(input)
  const error =
    parsed === undefined || (currency === 'HUF' && !Number.isInteger(parsed))
      ? currency === 'HUF'
        ? 'Egész forintösszeget adj meg.'
        : 'Érvényes összeget adj meg.'
      : parsed > MAX_DAILY_LIMIT
        ? `A napi limit legfeljebb ${formatMoney(MAX_DAILY_LIMIT, currency)} lehet.`
        : undefined
  const changed = value !== card.dailyLimit

  function handleSliderChange(next: number) {
    setValue(next)
    setInput(inputFormatter.format(next))
  }

  function handleInputChange(text: string) {
    setInput(text)
    const amount = parseAmount(text)
    if (amount !== undefined) {
      setValue(amount)
    }
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (error || !changed) {
      return
    }
    updateLimits.mutate(
      { id: card.id, request: { dailyLimit: value, onlinePaymentEnabled: card.onlinePaymentEnabled } },
      {
        onSuccess: () => toast.success('Mentettük az új napi limitet', { description: formatMoney(value, currency) }),
        onError: (mutationError) => toast.error(getErrorMessage(mutationError)),
      },
    )
  }

  return (
    <form noValidate onSubmit={handleSubmit}>
      <div className="flex items-baseline justify-between gap-4">
        <div>
          <p className="font-semibold text-foreground">Napi költési limit</p>
          <p className="mt-0.5 text-sm text-muted-foreground">Ennyit lehet a kártyával egy nap alatt költeni.</p>
        </div>
        <p className="shrink-0 text-lg font-extrabold tracking-tight text-foreground">{formatMoney(value, currency)}</p>
      </div>

      <Slider
        className="mt-4"
        aria-label="Napi költési limit"
        min={0}
        max={bounds.max}
        step={bounds.step}
        value={value}
        valueText={formatMoney(value, currency)}
        onValueChange={handleSliderChange}
        disabled={disabled}
      />
      <div aria-hidden className="mt-1 flex justify-between text-xs text-muted-foreground">
        <span>{formatMoney(0, currency)}</span>
        <span>{formatMoney(bounds.max, currency)}</span>
      </div>

      <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-start">
        <AmountField
          label="Pontos összeg"
          currency={currency}
          value={input}
          onChange={(event) => handleInputChange(event.target.value)}
          error={error}
          disabled={disabled}
          className="sm:max-w-56 sm:flex-1"
        />
        <Button type="submit" className="h-13 sm:mt-6.5" loading={updateLimits.isPending} disabled={disabled || !changed || error !== undefined}>
          Limit mentése
        </Button>
      </div>
    </form>
  )
}
