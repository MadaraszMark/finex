import type { AccountStatus, AccountType, CardStatus, Currency, StandingOrderFrequency, TransactionType } from '../api/types'

// Feltételes elválasztójel: a hosszú szó csak szűk helyen törik meg, kötőjellel
export const SHY = String.fromCharCode(0xad)

// A valódi mínuszjel (−) szebb, mint a kötőjel, amit az Intl a magyar formátumban használ
const MINUS = String.fromCharCode(0x2212)

// Pénznemenként egy formázó, mert az animált egyenleg képkockánként formáz
const moneyFormatters = new Map<string, Intl.NumberFormat>()

// Magyar formátum: "1 252 578 Ft", "1234,50 €" (a forint egész, a deviza két tizedes)
export function formatMoney(amount: number, currency: string): string {
  let formatter = moneyFormatters.get(currency)
  if (!formatter) {
    const fractionDigits = currency === 'HUF' ? 0 : 2
    formatter = new Intl.NumberFormat('hu-HU', {
      style: 'currency',
      currency,
      currencyDisplay: 'narrowSymbol',
      minimumFractionDigits: fractionDigits,
      maximumFractionDigits: fractionDigits,
    })
    moneyFormatters.set(currency, formatter)
  }
  return formatter.format(amount).replace('-', MINUS)
}

// Előjeles összeg (tételeknél): "+685 000 Ft", "−8990 Ft"
export function formatSignedMoney(signedAmount: number, currency: string): string {
  if (signedAmount > 0) {
    return `+${formatMoney(signedAmount, currency)}`
  }
  if (signedAmount < 0) {
    return `${MINUS}${formatMoney(-signedAmount, currency)}`
  }
  return formatMoney(0, currency)
}

// A tétel előjeles összege a típusából: a bevétel és a bejövő utalás pozitív, a többi negatív
export function signedAmount(type: TransactionType, amount: number): number {
  return isIncoming(type) ? amount : -amount
}

// Tengelyfeliratokhoz: "12,5 E", "1,3 M"
const compactFormatter = new Intl.NumberFormat('hu-HU', { notation: 'compact', maximumFractionDigits: 1 })

export function formatCompactNumber(value: number): string {
  return compactFormatter.format(value).replace('-', MINUS)
}

export function formatDateTime(isoDate: string): string {
  return new Intl.DateTimeFormat('hu-HU', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(isoDate))
}

// Hosszú dátum a hét napjával: "2026. szeptember 29., kedd"
export function formatLongDate(date: Date): string {
  return new Intl.DateTimeFormat('hu-HU', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }).format(date)
}

const shortDateFormatter = new Intl.DateTimeFormat('hu-HU', { month: 'short', day: 'numeric' })
const shortDateWithYearFormatter = new Intl.DateTimeFormat('hu-HU', { year: 'numeric', month: 'short', day: 'numeric' })
const timeFormatter = new Intl.DateTimeFormat('hu-HU', { hour: '2-digit', minute: '2-digit' })

// Rövid dátum: az idei évben "szept. 28.", korábban "2025. szept. 28."
export function formatShortDate(date: Date, now: Date): string {
  return date.getFullYear() === now.getFullYear() ? shortDateFormatter.format(date) : shortDateWithYearFormatter.format(date)
}

export function formatTime(isoDate: string): string {
  return timeFormatter.format(new Date(isoDate))
}

// Relatív idő a listákban: "Most", "12 perce", "3 órája", "Tegnap", "szept. 28."
export function formatRelativeTime(isoDate: string, now: Date): string {
  const date = new Date(isoDate)
  const minutes = Math.floor((now.getTime() - date.getTime()) / 60_000)

  if (minutes < 1) {
    return 'Most'
  }
  if (minutes < 60) {
    return `${minutes} perce`
  }
  if (date.toDateString() === now.toDateString()) {
    return `${Math.floor(minutes / 60)} órája`
  }
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return 'Tegnap'
  }
  return formatShortDate(date, now)
}

// "ÉÉÉÉ-HH-NN" (LocalDate) → helyi dátum (a new Date("2026-09-28") UTC éjfélként értelmezné)
export function parseIsoDate(isoDate: string): Date {
  const [year, month, day] = isoDate.split('-').map(Number)
  return new Date(year, month - 1, day)
}

// "ÉÉÉÉ-HH-NN" → "2026. szept. 28."
export function formatIsoDate(isoDate: string): string {
  return shortDateWithYearFormatter.format(parseIsoDate(isoDate))
}

// Havi összesítők hónapja ("2026-09") → "szept." vagy "2026. szeptember"
export function formatMonth(month: string, style: 'short' | 'long' = 'short'): string {
  const [year, monthIndex] = month.split('-').map(Number)
  const date = new Date(year, monthIndex - 1, 1)
  return style === 'short'
    ? new Intl.DateTimeFormat('hu-HU', { month: 'short' }).format(date)
    : new Intl.DateTimeFormat('hu-HU', { year: 'numeric', month: 'long' }).format(date)
}

// Négyes csoportokban, ahogy a kivonatokon szokás: HU56 1234 5678 …
export function formatIban(iban: string): string {
  return iban.replace(/(.{4})(?=.)/g, '$1 ')
}

// Rövidített számlaszám a kártyákra: "HU15 •••• 0001"
export function formatShortIban(iban: string): string {
  return `${iban.slice(0, 4)} •••• ${iban.slice(-4)}`
}

// A backend maszkolva küldi a kártyaszámot, itt csak pöttyökre cseréljük a csillagokat
export function formatMaskedCard(maskedNumber: string | undefined): string {
  return maskedNumber ? maskedNumber.replaceAll('*', '•') : 'Nincs kártya'
}

// A kártya lejárata a kártyákon megszokott HH/ÉÉ alakban: "2029-09-30" → "09/29"
export function formatCardExpiry(expiryDate: string): string {
  return `${expiryDate.slice(5, 7)}/${expiryDate.slice(2, 4)}`
}

// Szóközös ezres elválasztó és tizedesvessző is megengedett ("12 500,50")
export function parseAmount(input: string): number | undefined {
  const normalized = input.replace(/\s/g, '').replace(',', '.')
  if (!/^\d+(\.\d{1,2})?$/.test(normalized)) {
    return undefined
  }
  return Number(normalized)
}

// Nap a listák fejlécében: "Ma", "Tegnap", "szeptember 28., vasárnap"
export function formatDayHeading(date: Date, now: Date): string {
  if (date.toDateString() === now.toDateString()) {
    return 'Ma'
  }
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return 'Tegnap'
  }
  return new Intl.DateTimeFormat('hu-HU', {
    year: date.getFullYear() === now.getFullYear() ? undefined : 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  }).format(date)
}

// Jövőbeli nap viszonyítva: "ma", "holnap", "5 nap múlva"
export function formatDaysUntil(isoDate: string, now: Date): string {
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const days = Math.round((parseIsoDate(isoDate).getTime() - today.getTime()) / 86_400_000)
  if (days <= 0) {
    return 'ma'
  }
  if (days === 1) {
    return 'holnap'
  }
  return `${days} nap múlva`
}

// Monogram egy teljes névből (pl. kedvezményezett): "Tóth Gábor" → "TG", "MVM Next" → "MN"
export function nameInitials(name: string): string {
  const words = name.trim().split(/\s+/).filter(Boolean)
  return words
    .slice(0, 2)
    .map((word) => word.charAt(0))
    .join('')
    .toUpperCase()
}

// Monogram a magyar névsorrendben: Kovács Anna → "KA"
export function initials(lastName: string | undefined, firstName: string | undefined): string {
  return `${lastName?.charAt(0) ?? ''}${firstName?.charAt(0) ?? ''}`.toUpperCase()
}

export function isIncoming(type: TransactionType): boolean {
  return type === 'INCOME' || type === 'TRANSFER_IN'
}

// Böngészőfül címe, elöl az olvasatlan értesítések számával: "(3) Főoldal · FineX"
export function formatTabTitle(title: string | undefined, unread: number): string {
  const base = title ? `${title.replaceAll(SHY, '')} · FineX` : 'FineX'
  return unread > 0 ? `(${unread > 99 ? '99+' : unread}) ${base}` : base
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

export const accountTypeLabels: Record<AccountType, string> = {
  CURRENT: 'Folyószámla',
  SAVINGS: 'Megtakarítási számla',
  CREDIT: 'Hitelszámla',
}

export const frequencyLabels: Record<StandingOrderFrequency, string> = {
  WEEKLY: 'Hetente',
  MONTHLY: 'Havonta',
}

export const cardStatusLabels: Record<CardStatus, string> = {
  ACTIVE: 'Aktív',
  BLOCKED: 'Letiltva',
  CANCELLED: 'Megszűnt',
}

export const currencies: Currency[] = ['HUF', 'EUR', 'USD']

export const currencyNames: Record<Currency, string> = {
  HUF: 'Forint',
  EUR: 'Euró',
  USD: 'Dollár',
}

export function currencyName(currency: string): string {
  return currencyNames[currency as Currency] ?? currency
}

const currencySymbols: Record<Currency, string> = {
  HUF: 'Ft',
  EUR: '€',
  USD: '$',
}

export function currencySymbol(currency: string): string {
  return currencySymbols[currency as Currency] ?? currency
}
