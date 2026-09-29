import { ArrowLeftRight, ArrowUpRight, ChartColumn, ChartPie, CreditCard, PiggyBank, Send, Sparkles, Wallet } from 'lucide-react'
import { motion } from 'motion/react'
import { useState } from 'react'
import { Link } from 'react-router'
import { cardSurface } from '../../components/ui/cardStyles'
import PageHeader from '../../components/ui/PageHeader'
import { cn } from '../../lib/cn'
import { formatLongDate, SHY } from '../../lib/format'
import { cardReveal, staggerContainer, staggerItem } from '../../lib/motion'
import { useCurrentUser } from '../../session/useCurrentUser'
import CardSpotlight from './CardSpotlight'

const MotionLink = motion.create(Link)

// Ami a 2. részben a főoldalra kerül
const previews = [
  { icon: Wallet, title: 'Egyenlegek', description: 'Minden számlád egy helyen' },
  { icon: ChartColumn, title: 'Bevétel és kiadás', description: 'A hónap összesítve' },
  { icon: ArrowLeftRight, title: 'Legutóbbi tételek', description: 'A friss pénzmozgásaid' },
  { icon: CreditCard, title: 'Kártyáid', description: 'Limit és mai költés' },
]

const shortcuts = [
  { label: 'Utalás', description: 'Pénzküldés pár lépésben', to: '/transfer', icon: Send },
  { label: 'Számlák', description: 'Egyenlegek, kivonatok', to: '/accounts', icon: Wallet },
  { label: 'Kártyák', description: 'Limit, tiltás, feloldás', to: '/cards', icon: CreditCard },
  { label: 'Tranzakciók', description: 'Keresés és szűrés', to: '/transactions', icon: ArrowLeftRight },
  { label: `Megtaka${SHY}rítások`, description: 'Célok és kamat', to: '/savings', icon: PiggyBank },
  { label: 'Statisztika', description: 'Mire megy el a pénzed?', to: '/statistics', icon: ChartPie },
]

// Főoldal – az 1. részben üdvözlés, a saját bankkártyád és gyorslinkek; az egyenlegek és a tranzakciók a 2. részben jönnek
export default function HomePage() {
  const { data: user } = useCurrentUser()
  const [today] = useState(() => formatLongDate(new Date()))

  return (
    <>
      <PageHeader
        eyebrow={today}
        title={user ? 'Szia,' : 'Szia!'}
        highlight={user ? `${user.firstName}!` : undefined}
        description="Örülünk, hogy újra itt vagy."
      />

      <div className="grid items-center gap-8 lg:grid-cols-[minmax(0,1fr)_24rem] lg:gap-6">
        {/* Mobilon a kártya van elöl, közvetlenül a lila fejlécsáv alatt */}
        <CardSpotlight className="lg:order-last" />

        <motion.section variants={cardReveal} initial="hidden" animate="show" className={cn(cardSurface, 'relative overflow-hidden p-6 sm:p-8')}>
          <div
            aria-hidden
            className="pointer-events-none absolute -top-24 -right-24 size-72 rounded-full bg-brand-400/20 blur-3xl motion-safe:animate-aurora-2 dark:bg-brand-600/20"
          />

          <motion.p
            variants={staggerItem}
            className="relative inline-flex items-center gap-1.5 rounded-full bg-brand-100 px-3 py-1 text-xs font-bold text-brand-700 dark:bg-brand-950 dark:text-brand-300"
          >
            <Sparkles className="size-3.5" />
            Hamarosan · a frontend 2. részében
          </motion.p>
          <motion.h2 variants={staggerItem} className="relative mt-4 text-2xl font-extrabold tracking-tight sm:text-3xl">
            Itt látod majd a pénzügyeid áttekintését
          </motion.h2>
          <motion.p variants={staggerItem} className="relative mt-2 text-muted-foreground">
            Egy pillantással minden, ami számít.
          </motion.p>

          <div className="relative mt-6 grid gap-3 sm:grid-cols-2">
            {previews.map((preview) => (
              <motion.div key={preview.title} variants={staggerItem} className="flex items-center gap-3 rounded-2xl bg-muted/70 p-3">
                <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-card text-primary shadow-sm">
                  <preview.icon className="size-5" />
                </span>
                <div className="min-w-0">
                  <p className="text-sm font-bold">{preview.title}</p>
                  <p className="truncate text-xs text-muted-foreground">{preview.description}</p>
                </div>
              </motion.div>
            ))}
          </div>
        </motion.section>
      </div>

      <h2 className="mt-10 mb-4 text-lg font-bold">Gyors elérés</h2>
      <motion.div variants={staggerContainer} initial="hidden" animate="show" className="grid grid-cols-2 gap-3 sm:grid-cols-3 sm:gap-4">
        {shortcuts.map((shortcut) => (
          <MotionLink
            key={shortcut.to}
            to={shortcut.to}
            variants={staggerItem}
            whileHover={{ y: -4 }}
            whileTap={{ scale: 0.98 }}
            className={cn(
              cardSurface,
              'group flex flex-col p-4 transition-[border-color,box-shadow] duration-300 sm:p-5',
              'hover:border-brand-300 hover:shadow-[0_22px_40px_-24px_rgb(124_69_209/0.6)] dark:hover:border-brand-500/40',
              'focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none',
            )}
          >
            <div className="flex items-start justify-between">
              <span className="flex size-11 items-center justify-center rounded-2xl bg-brand-100 text-brand-600 transition duration-300 group-hover:scale-110 group-hover:bg-brand-gradient group-hover:text-white dark:bg-brand-950 dark:text-brand-300">
                <shortcut.icon className="size-5" />
              </span>
              <ArrowUpRight
                aria-hidden
                className="size-5 text-muted-foreground opacity-0 transition duration-300 group-hover:translate-x-0.5 group-hover:-translate-y-0.5 group-hover:opacity-100"
              />
            </div>
            <p className="mt-4 font-bold text-foreground">{shortcut.label}</p>
            <p className="mt-0.5 text-sm text-muted-foreground">{shortcut.description}</p>
          </MotionLink>
        ))}
      </motion.div>
    </>
  )
}
