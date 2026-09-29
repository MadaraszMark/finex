import { useQuery } from '@tanstack/react-query'
import { motion, useTransform } from 'motion/react'
import { getMyCards } from '../../api/cards'
import { queryKeys } from '../../api/queryKeys'
import FinexCard from '../../components/brand/FinexCard'
import { usePointerParallax } from '../../hooks/usePointerParallax'
import { cn } from '../../lib/cn'
import { formatCardExpiry, formatMaskedCard } from '../../lib/format'
import { useCurrentUser } from '../../session/useCurrentUser'

// A felhasználó (első aktív) bankkártyája lebegő, az egér mozgására térben megdőlő grafikaként
export default function CardSpotlight({ className }: { className?: string }) {
  const { data: user } = useCurrentUser()
  const { data: cards } = useQuery({ queryKey: queryKeys.cards, queryFn: getMyCards })
  const card = cards?.find((item) => item.status === 'ACTIVE') ?? cards?.[0]

  const pointer = usePointerParallax()
  const rotateX = useTransform(pointer.y, (value) => value * -14)
  const rotateY = useTransform(pointer.x, (value) => value * 18)

  const holderName = card?.holderName ?? (user ? `${user.lastName} ${user.firstName}`.toLocaleUpperCase('hu') : '')
  const maskedNumber = card ? formatMaskedCard(card.maskedNumber) : '•••• •••• •••• ••••'
  const expiry = card ? formatCardExpiry(card.expiryDate) : '••/••'

  return (
    <div
      role="img"
      aria-label={card ? `Bankkártyád: ${maskedNumber}, lejárat: ${expiry}` : 'Bankkártyád'}
      className={cn('relative mx-auto w-full max-w-[22rem] [perspective:1200px]', className)}
    >
      {/* Mögötte egy elfordított, áttetsző "második kártya", mint a belépő oldalon */}
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
          <FinexCard variant="solid" holderName={holderName} maskedNumber={maskedNumber} expiry={expiry} />
        </div>
      </motion.div>
    </div>
  )
}
