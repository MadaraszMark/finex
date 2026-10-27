import { z } from 'zod'
import { amountSchema } from '../accounts/schemas'
import { ibanField, messageField, partnerNameField } from '../transfer/schemas'

// A backend szabályai; a dátum nem lehet múltbeli, ezért a séma a mai napot is megkapja
export function standingOrderSchema(currency: string, today: string) {
  return z.object({
    accountId: z.string().min(1, 'Válaszd ki, melyik számláról menjen az utalás.'),
    toAccountNumber: ibanField,
    partnerName: partnerNameField,
    amount: amountSchema(currency, 10_000_000, 'Egy utalás legfeljebb 10 000 000 lehet.'),
    message: messageField,
    frequency: z.enum(['WEEKLY', 'MONTHLY']),
    date: z
      .string()
      .regex(/^\d{4}-\d{2}-\d{2}$/, 'Add meg a dátumot.')
      .refine((date) => date >= today, 'A dátum nem lehet a múltban.'),
  })
}

export type StandingOrderForm = z.infer<ReturnType<typeof standingOrderSchema>>
