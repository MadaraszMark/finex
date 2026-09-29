import type { AccountStatus, TransactionType } from '../api/types'

// Feltételes elválasztójel (soft hyphen) hosszú, egyszavas feliratokba (pl. `Megtaka${SHY}rítások`): a szó csak
// szűk helyen (pl. mobilos csempéken) törik meg ott, kötőjellel; máshol nem látszik
export const SHY = String.fromCharCode(0xad)

export function formatMoney(amount: number, currency: string): string {
  const fractionDigits = currency === 'HUF' ? 0 : 2
  return new Intl.NumberFormat('hu-HU', {
    style: 'currency',
    currency,
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  }).format(amount)
}

export function formatDateTime(isoDate: string): string {
  return new Intl.DateTimeFormat('hu-HU', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(isoDate))
}

// Hosszú dátum a hét napjával: "2026. szeptember 29., kedd"
export function formatLongDate(date: Date): string {
  return new Intl.DateTimeFormat('hu-HU', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }).format(date)
}

// Négyes csoportokban, ahogy a kivonatokon szokás: HU56 1234 5678 …
export function formatIban(iban: string): string {
  return iban.replace(/(.{4})(?=.)/g, '$1 ')
}

// A backend már maszkolva küldi a kártyaszámot ("**** **** **** 1184"), itt csak pöttyökre cseréljük a csillagokat
export function formatMaskedCard(maskedNumber: string | undefined): string {
  return maskedNumber ? maskedNumber.replaceAll('*', '•') : 'Nincs kártya'
}

// A kártya lejárata a kártyákon megszokott HH/ÉÉ alakban: "2029-09-30" → "09/29"
export function formatCardExpiry(expiryDate: string): string {
  return `${expiryDate.slice(5, 7)}/${expiryDate.slice(2, 4)}`
}

// Monogram a magyar névsorrendben: Kovács Anna → "KA"
export function initials(lastName: string | undefined, firstName: string | undefined): string {
  return `${lastName?.charAt(0) ?? ''}${firstName?.charAt(0) ?? ''}`.toUpperCase()
}

export function isIncoming(type: TransactionType): boolean {
  return type === 'INCOME' || type === 'TRANSFER_IN'
}

export const transactionTypeLabels: Record<TransactionType, string> = {
  INCOME: 'Bevétel',
  OUTCOME: 'Kiadás',
  TRANSFER_IN: 'Bejövő utalás',
  TRANSFER_OUT: 'Kimenő utalás',
}

export const accountStatusLabels: Record<AccountStatus, string> = {
  ACTIVE: 'Aktív',
  BLOCKED: 'Letiltva',
  CLOSED: 'Lezárva',
  FROZEN: 'Befagyasztva',
}
