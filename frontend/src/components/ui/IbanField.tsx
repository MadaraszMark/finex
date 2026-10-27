import { CircleCheck } from 'lucide-react'
import TextField, { type TextFieldProps } from './TextField'

// IBAN mező: a pipa a formai érvényességet jelzi; a szóközt és a kisbetűt a backend normalizálja
export default function IbanField({ valid = false, ...props }: Omit<TextFieldProps, 'rightSlot'> & { valid?: boolean }) {
  return (
    <TextField
      autoComplete="off"
      autoCapitalize="characters"
      spellCheck={false}
      placeholder="HU00 0000 0000 0000 0000 0000 0000"
      rightSlot={
        <span className="flex size-10 items-center justify-center text-emerald-600 dark:text-emerald-400">
          {valid && <CircleCheck aria-label="Érvényes számlaszám" className="size-5" />}
        </span>
      }
      {...props}
    />
  )
}
