import { ArrowLeft, ArrowLeftRight, Headset, Send, Wallet } from 'lucide-react'
import { motion } from 'motion/react'
import { Link } from 'react-router'
import ButtonLink from '../../components/ui/ButtonLink'
import { buttonStyles } from '../../components/ui/buttonStyles'
import { cardSurface } from '../../components/ui/cardStyles'
import PageHeader from '../../components/ui/PageHeader'
import { cn } from '../../lib/cn'
import { cardReveal, staggerItem } from '../../lib/motion'

// Gyakran keresett oldalak, ha a cím elgépelt vagy elavult
const suggestions = [
  { label: 'Számlák', to: '/accounts', icon: Wallet },
  { label: 'Utalás', to: '/transfer', icon: Send },
  { label: 'Tranzakciók', to: '/transactions', icon: ArrowLeftRight },
  { label: 'Ügyfélszolgálat', to: '/support', icon: Headset },
]

export default function NotFoundPage() {
  return (
    <>
      <PageHeader eyebrow="404-es hiba" title="Ez az oldal nem található" description="Lehet, hogy elgépelted a címet, vagy az oldal már nem létezik." />

      <motion.section variants={cardReveal} initial="hidden" animate="show" className={cn(cardSurface, 'px-6 py-10 text-center sm:px-10 sm:py-12')}>
        <motion.p variants={staggerItem} aria-hidden className="text-8xl font-extrabold tracking-tighter sm:text-9xl">
          <span className="text-brand-gradient inline-block motion-safe:animate-float">404</span>
        </motion.p>
        <motion.p variants={staggerItem} className="mt-6 font-semibold">
          Talán ezeket keresed:
        </motion.p>
        <motion.div variants={staggerItem} className="mt-4 flex flex-wrap justify-center gap-2">
          {suggestions.map((item) => (
            <Link key={item.to} to={item.to} className={buttonStyles({ variant: 'outline', size: 'sm' })}>
              <item.icon />
              {item.label}
            </Link>
          ))}
        </motion.div>
        <motion.div variants={staggerItem} className="mt-8">
          <ButtonLink to="/" leftIcon={<ArrowLeft />}>
            Vissza a főoldalra
          </ButtonLink>
        </motion.div>
      </motion.section>
    </>
  )
}
