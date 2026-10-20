import { z } from 'zod'
import { parseAmount } from '../../lib/format'

// Az űrlapok szabályai a backend validációját követik (CreateAccountRequest, DepositRequest)

export const openAccountSchema = z.object({
  name: z.string().trim().min(1, 'Add meg a számla nevét.').max(100, 'Legfeljebb 100 karakter lehet.'),
  currency: z.enum(['HUF', 'EUR', 'USD']),
})

export type OpenAccountForm = z.infer<typeof openAccountSchema>

// Összegmező: a beírt szöveget értelmezi (szóköz, tizedesvessző); forintnál csak egész összeg lehet
export function amountSchema(currency: string, max: number, maxMessage: string) {
  return z
    .string()
    .trim()
    .min(1, 'Add meg az összeget.')
    .superRefine((value, context) => {
      const amount = parseAmount(value)
      if (amount === undefined || (currency === 'HUF' && !Number.isInteger(amount))) {
        context.addIssue({
          code: 'custom',
          message: currency === 'HUF' ? 'Egész forintösszeget adj meg (pl. 25 000).' : 'Érvényes összeget adj meg (pl. 120,50).',
        })
      } else if (amount <= 0) {
        context.addIssue({ code: 'custom', message: 'Az összeg legyen nagyobb nullánál.' })
      } else if (amount > max) {
        context.addIssue({ code: 'custom', message: maxMessage })
      }
    })
}

export function depositSchema(currency: string) {
  return z.object({
    amount: amountSchema(currency, 10_000_000, 'Egy befizetés legfeljebb 10 000 000 lehet.'),
    message: z.string().trim().max(255, 'Legfeljebb 255 karakter lehet.'),
  })
}

export type DepositForm = z.infer<ReturnType<typeof depositSchema>>

// Gyors összegek a befizetéshez és a demó fizetéshez, pénznemenként
export const quickAmounts: Record<string, number[]> = {
  HUF: [10_000, 25_000, 50_000, 100_000],
  EUR: [20, 50, 100, 250],
  USD: [20, 50, 100, 250],
}
