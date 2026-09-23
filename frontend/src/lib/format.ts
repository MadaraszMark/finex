import type { AccountStatus, TransactionType } from '../api/types'

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

// Négyes csoportokban, ahogy a kivonatokon szokás: HU56 1234 5678 …
export function formatIban(iban: string): string {
  return iban.replace(/(.{4})(?=.)/g, '$1 ')
}

// A kártyaszámból csak az utolsó 4 számjegy látszik
export function maskCardNumber(cardNumber: string | null): string {
  return cardNumber ? `•••• •••• •••• ${cardNumber.slice(-4)}` : 'Nincs kártya'
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
