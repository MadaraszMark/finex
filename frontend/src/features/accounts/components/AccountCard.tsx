import { motion } from 'motion/react'
import { Link } from 'react-router'
import type { AccountResponse } from '../../../api/types'
import { DotGrid, Waves } from '../../../components/brand/Decorations'
import { cn } from '../../../lib/cn'
import { accountStatusLabels, accountTypeLabels, formatMoney, formatShortIban } from '../../../lib/format'
import { accountTheme } from '../accountTheme'

const MotionLink = motion.create(Link)

// Számlakártya a koncepcióképek stílusában (pénznemenként más színátmenet, hullám- és pöttydíszítéssel);
// a számlaadatlapra visz. A lezárt számla szürke. Rácsban a sor magasságát kitöltheti (h-full): az egyenleg ilyenkor
// is a kártya aljára kerül, így a szomszédos kártyákon egy vonalban áll.
export default function AccountCard({ account, className }: { account: AccountResponse; className?: string }) {
  const theme = accountTheme(account)

  return (
    <MotionLink
      to={`/accounts/${account.id}`}
      whileHover={{ y: -4 }}
      whileTap={{ scale: 0.98 }}
      className={cn(
        'spotlight relative isolate flex flex-col overflow-hidden rounded-[1.75rem] p-6 text-white [--spot-color:rgb(255_255_255/0.22)]',
        'focus-visible:ring-4 focus-visible:ring-ring/50 focus-visible:outline-none',
        theme.surface,
        theme.glow,
        className,
      )}
    >
      <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-12 -right-10 size-40 rounded-full bg-white/15" />
        <Waves className="absolute top-16 left-5 w-12 text-white/35" />
        <DotGrid className="absolute right-6 bottom-5 w-16 text-white/30" rows={4} columns={5} />
      </div>

      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate font-bold">{account.name}</p>
          <p className="text-xs font-medium text-white/70">{accountTypeLabels[account.accountType]}</p>
        </div>
        <span className="shrink-0 rounded-full border border-white/30 bg-white/15 px-2.5 py-1 text-xs font-bold backdrop-blur-md">
          {account.currency}
        </span>
      </div>

      <div className="mt-auto pt-9">
        <p className="text-3xl font-extrabold tracking-tight">{formatMoney(account.balance, account.currency)}</p>
        <div className="mt-2 flex items-center justify-between gap-3 text-sm text-white/75">
          <span className="font-mono tracking-wider">{formatShortIban(account.accountNumber)}</span>
          {account.status !== 'ACTIVE' && (
            <span className="rounded-full bg-black/25 px-2.5 py-0.5 text-xs font-bold text-white">{accountStatusLabels[account.status]}</span>
          )}
        </div>
      </div>
    </MotionLink>
  )
}
