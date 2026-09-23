import type { AccountResponse } from '../api/types'
import { accountStatusLabels, formatIban, formatMoney, maskCardNumber } from '../lib/format'

// A koncepcióképek "Fő számla" kártyája
export default function AccountCard({ account }: { account: AccountResponse }) {
  return (
    <div className="relative overflow-hidden rounded-3xl bg-linear-to-br from-brand-400 via-brand-500 to-brand-700 p-6 text-white shadow-lg shadow-brand-500/30">
      <div aria-hidden className="absolute -top-12 -right-12 size-44 rounded-full bg-white/10" />
      <div aria-hidden className="absolute -bottom-20 -left-10 size-52 rounded-full bg-white/5" />

      <div className="relative">
        <div className="flex items-center justify-between">
          <p className="text-sm font-medium text-white/80">Fő számla</p>
          <span className="rounded-full bg-white/20 px-3 py-1 text-xs font-semibold">
            {accountStatusLabels[account.status]}
          </span>
        </div>

        <p className="mt-5 text-4xl font-bold tracking-tight">{formatMoney(account.balance, account.currency)}</p>

        <div className="mt-8 space-y-1 font-mono text-sm tracking-wider">
          <p className="text-white/90">{formatIban(account.accountNumber)}</p>
          <p className="text-white/70">{maskCardNumber(account.cardNumber)}</p>
        </div>
      </div>
    </div>
  )
}
