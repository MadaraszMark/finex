import { currencySymbol } from '../../lib/format'
import TextField, { type TextFieldProps } from './TextField'

interface AmountFieldProps extends Omit<TextFieldProps, 'rightSlot' | 'inputMode' | 'type'> {
  currency: string
}

// Összegmező: mobilon számbillentyűzet, a mező végén a pénznem jele. A szöveget a zod-séma értelmezi
// (szóköz ezres elválasztó és tizedesvessző is megengedett: "12 500", "120,50").
export default function AmountField({ currency, ...props }: AmountFieldProps) {
  return (
    <TextField
      inputMode="decimal"
      autoComplete="off"
      rightSlot={<span className="pr-3 text-sm font-semibold text-muted-foreground">{currencySymbol(currency)}</span>}
      {...props}
    />
  )
}
