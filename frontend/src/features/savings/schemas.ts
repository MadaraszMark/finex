import { z } from 'zod'
import { parseAmount } from '../../lib/format'
import { amountSchema } from '../accounts/schemas'

// Az űrlapok szabályai a backend validációját követik (CreateSavingsAccountRequest, UpdateSavingsAccountRequest,
// SavingsTransferRequest). Az összegek pénzneme a kiválasztott folyószámláé, ezért a sémák pénznemenként készülnek.

const nameField = z.string().trim().min(1, 'Add meg, mire gyűjtesz.').max(100, 'Legfeljebb 100 karakter lehet.')

// Nem kötelező összeg (pl. célösszeg, kezdő befizetés): üresen maradhat, egyébként érvényes összeg kell
function optionalAmount(currency: string, min: number, minMessage: string) {
  return z
    .string()
    .trim()
    .superRefine((value, context) => {
      if (!value) {
        return
      }
      const amount = parseAmount(value)
      if (amount === undefined || (currency === 'HUF' && !Number.isInteger(amount))) {
        context.addIssue({ code: 'custom', message: currency === 'HUF' ? 'Egész forintösszeget adj meg.' : 'Érvényes összeget adj meg.' })
      } else if (amount < min) {
        context.addIssue({ code: 'custom', message: minMessage })
      } else if (amount > 100_000_000) {
        context.addIssue({ code: 'custom', message: 'Ez túl nagy összeg.' })
      }
    })
}

export function createSavingsSchema(currency: string) {
  return z.object({
    name: nameField,
    accountId: z.string().min(1, 'Válaszd ki, melyik számládhoz tartozzon.'),
    initialDeposit: optionalAmount(currency, 0, 'A kezdő összeg nem lehet negatív.'),
    targetAmount: optionalAmount(currency, 1, 'A célösszeg legalább 1 legyen.'),
  })
}

export type CreateSavingsForm = z.infer<ReturnType<typeof createSavingsSchema>>

export function updateSavingsSchema(currency: string) {
  return z.object({
    name: nameField,
    targetAmount: optionalAmount(currency, 1, 'A célösszeg legalább 1 legyen.'),
  })
}

export type UpdateSavingsForm = z.infer<ReturnType<typeof updateSavingsSchema>>

export function savingsTransferSchema(currency: string) {
  return z.object({
    accountId: z.string().min(1, 'Válassz folyószámlát.'),
    amount: amountSchema(currency, 10_000_000, 'Egyszerre legfeljebb 10 000 000 mozgatható.').refine(
      (value) => (parseAmount(value) ?? 0) >= 1,
      'Legalább 1 egységet adj meg.',
    ),
    message: z.string().trim().max(255, 'Legfeljebb 255 karakter lehet.'),
  })
}

export type SavingsTransferForm = z.infer<ReturnType<typeof savingsTransferSchema>>

// A következő havi kamat becslése a backend képletével: egyenleg × éves kamatláb / 12 / 100
export function estimatedMonthlyInterest(balance: number, interestRate: number): number {
  return Math.round(((balance * interestRate) / 1200) * 100) / 100
}
