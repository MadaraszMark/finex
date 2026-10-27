import { ArrowRight } from 'lucide-react'
import { motion, useTransform } from 'motion/react'
import { Link } from 'react-router'
import FinexCard from '../../components/brand/FinexCard'
import { Card, CardHeader } from '../../components/ui/Card'
import Meter from '../../components/ui/Meter'
import Skeleton from '../../components/ui/Skeleton'
import { usePointerParallax } from '../../hooks/usePointerParallax'
import { cn } from '../../lib/cn'
import { formatCardExpiry, formatMaskedCard, formatMoney } from '../../lib/format'
import { accountTheme } from '../accounts/accountTheme'
import { useAccounts } from '../accounts/queries'
import { useCards } from '../cards/queries'

// A főoldal kártyablokkja: az első aktív bankkártya és a mai költés a napi limithez képest
export default function CardSpotlight({ className }: { className?: string }) {
  const { data: cards } = useCards()
  const { data: accounts } = useAccounts()
  const card = cards?.find((item) => item.status === 'ACTIVE') ?? cards?.find((item) => item.status !== 'CANCELLED')
  const account = accounts?.find((item) => item.id === card?.accountId)

  const pointer = usePointerParallax()
  const rotateX = useTransform(pointer.y, (value) => value * -14)
  const rotateY = useTransform(pointer.x, (value) => value * 18)

  return (
    <Card className={cn('flex flex-col p-5 sm:p-6', className)}>
      <CardHeader
        title="Bankkártyád"
        description={account?.name}
        action={
          card && (
            <Link
              to={`/cards?card=${card.id}`}
              className="group inline-flex items-center gap-1 rounded-lg text-sm font-semibold text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
            >
              Kezelés
              <ArrowRight aria-hidden className="size-4 transition-transform group-hover:translate-x-0.5" />
            </Link>
          )
        }
      />

      {!cards || !accounts ? (
        <Skeleton className="mx-auto mt-6 h-56 max-w-[22rem] rounded-[1.75rem]" />
      ) : !card ? (
        <p className="mt-6 text-sm text-muted-foreground">Nincs aktív bankkártyád.</p>
      ) : (
        <>
          <div className="flex flex-1 flex-col justify-center">
            <div
              role="img"
              aria-label={`Bankkártyád: ${formatMaskedCard(card.maskedNumber)}, lejárat: ${formatCardExpiry(card.expiryDate)}`}
              className="relative mx-auto mt-7 w-full max-w-[22rem] [perspective:1200px]"
            >
              <motion.div
                style={{ rotateX, rotateY }}
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.15, duration: 0.6 }}
                className="absolute inset-x-8 -top-4 h-52 -rotate-6 rounded-[1.75rem] border border-white/20 bg-brand-900/30 dark:bg-brand-700/25"
              />
              <motion.div
                style={{ rotateX, rotateY }}
                initial={{ opacity: 0, y: 40, rotate: -8 }}
                animate={{ opacity: 1, y: 0, rotate: 0 }}
                transition={{ delay: 0.25, type: 'spring', stiffness: 80, damping: 14 }}
                className="relative h-56"
              >
                <div className="size-full motion-safe:animate-float">
                  <FinexCard
                    variant="solid"
                    holderName={card.holderName}
                    maskedNumber={formatMaskedCard(card.maskedNumber)}
                    expiry={formatCardExpiry(card.expiryDate)}
                    status={card.status}
                    className={account ? accountTheme({ currency: account.currency, status: 'ACTIVE' }).surface : undefined}
                  />
                </div>
              </motion.div>
            </div>
          </div>

          <div className="mt-8">
            <div className="flex items-baseline justify-between gap-3 text-sm">
              <span className="font-medium text-muted-foreground">Mai költés</span>
              <span className="text-muted-foreground">
                <span className="font-bold text-foreground">{formatMoney(card.spentToday, account?.currency ?? 'HUF')}</span> /{' '}
                {formatMoney(card.dailyLimit, account?.currency ?? 'HUF')}
              </span>
            </div>
            <Meter
              className="mt-2.5"
              label="Mai költés a napi limithez képest"
              value={card.spentToday}
              max={card.dailyLimit}
              valueText={`${formatMoney(card.spentToday, account?.currency ?? 'HUF')} a ${formatMoney(card.dailyLimit, account?.currency ?? 'HUF')} napi limitből`}
            />
          </div>
        </>
      )}
    </Card>
  )
}
