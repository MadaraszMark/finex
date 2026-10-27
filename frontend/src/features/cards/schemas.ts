import { z } from 'zod'
import { amountSchema } from '../accounts/schemas'

// A napi limit felső határa; a csúszka csak a szokásos tartományt fedi le
export const MAX_DAILY_LIMIT = 5_000_000

export function limitSliderBounds(currency: string): { max: number; step: number } {
  return currency === 'HUF' ? { max: 2_000_000, step: 10_000 } : { max: 5_000, step: 50 }
}

export function cardPaymentSchema(currency: string) {
  return z.object({
    merchantName: z.string().trim().min(1, 'Add meg, hol fizetsz.').max(150, 'Legfeljebb 150 karakter lehet.'),
    amount: amountSchema(currency, 10_000_000, 'Egy fizetés legfeljebb 10 000 000 lehet.'),
    categoryId: z.string(),
    online: z.boolean(),
  })
}

export type CardPaymentForm = z.infer<ReturnType<typeof cardPaymentSchema>>

// Gyors kitöltés a demó fizetéshez (a kategória név szerint a törzsadatból)
export const paymentPresets = [
  { merchantName: 'Tesco', categoryName: 'Élelmiszer', online: false },
  { merchantName: 'Wolt', categoryName: 'Étterem, kávézó', online: true },
  { merchantName: 'Netflix', categoryName: 'Szórakozás', online: true },
  { merchantName: 'MÁV', categoryName: 'Közlekedés', online: false },
]
