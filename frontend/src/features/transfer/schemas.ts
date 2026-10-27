import { z } from 'zod'
import { formatMoney } from '../../lib/format'
import { isValidIban } from '../../lib/iban'
import { amountSchema } from '../accounts/schemas'

// Közös mezőszabályok: utalás, kedvezményezett és rendszeres átutalás
export const ibanField = z
  .string()
  .trim()
  .min(1, 'Add meg a számlaszámot.')
  .max(42, 'Legfeljebb 42 karakter lehet.')
  .refine(isValidIban, 'Hibás számlaszám (IBAN) – ellenőrizd a számjegyeket.')

export const partnerNameField = z.string().trim().min(1, 'Add meg a címzett nevét.').max(150, 'Legfeljebb 150 karakter lehet.')

export const messageField = z.string().trim().max(255, 'Legfeljebb 255 karakter lehet.')

// Pénznemenként készül; a napi limitet (GET /settings) betöltés előtt csak a backend ellenőrzi
export function transferSchema(currency: string, dailyLimit: number | undefined) {
  return z.object({
    fromAccountId: z.string().min(1, 'Válaszd ki, melyik számládról utalsz.'),
    toAccountNumber: ibanField,
    partnerName: partnerNameField,
    amount: amountSchema(
      currency,
      dailyLimit ?? Number.POSITIVE_INFINITY,
      dailyLimit !== undefined ? `Naponta legfeljebb ${formatMoney(dailyLimit, currency)} utalható egy számláról.` : '',
    ),
    message: messageField,
    categoryId: z.string(),
    saveBeneficiary: z.boolean(),
  })
}

export type TransferForm = z.infer<ReturnType<typeof transferSchema>>
