import { Check, Sparkles, type LucideIcon } from 'lucide-react'
import { motion } from 'motion/react'
import { cardSurface } from '../../components/ui/cardStyles'
import PageHeader from '../../components/ui/PageHeader'
import { cn } from '../../lib/cn'
import { cardReveal, staggerItem } from '../../lib/motion'
import type { PlannedPage } from './plannedPages'

interface ComingSoonPageProps extends PlannedPage {
  title: string
  icon: LucideIcon
}

// Ideiglenes oldal a még el nem készült menüpontokhoz: megmutatja, mi fog itt szerepelni
export default function ComingSoonPage({ title, icon: Icon, part, description, features }: ComingSoonPageProps) {
  return (
    <>
      <PageHeader title={title} description={description} />

      <motion.section variants={cardReveal} initial="hidden" animate="show" className={cn(cardSurface, 'relative overflow-hidden p-6 sm:p-8')}>
        <div
          aria-hidden
          className="pointer-events-none absolute -top-24 -right-24 size-72 rounded-full bg-brand-400/20 blur-3xl motion-safe:animate-aurora-2 dark:bg-brand-600/20"
        />

        <div className="relative flex flex-col gap-6 sm:flex-row sm:items-start">
          <motion.span variants={staggerItem} className="relative flex size-16 shrink-0 items-center justify-center rounded-3xl bg-brand-gradient text-white shadow-glow">
            {/* Az ikon körül lassan "lélegző" gyűrű */}
            <span aria-hidden className="absolute inset-0 rounded-3xl ring-2 ring-brand-400/70 motion-safe:animate-pulse-ring" />
            <Icon className="size-7" />
          </motion.span>
          <div>
            <motion.p
              variants={staggerItem}
              className="inline-flex items-center gap-1.5 rounded-full bg-brand-100 px-3 py-1 text-xs font-bold text-brand-700 dark:bg-brand-950 dark:text-brand-300"
            >
              <Sparkles className="size-3.5" />
              Hamarosan · a frontend {part}. részében készül el
            </motion.p>
            <motion.h2 variants={staggerItem} className="mt-4 text-xl font-bold">
              Ami itt várható
            </motion.h2>
            <ul className="mt-3 space-y-2.5">
              {features.map((feature) => (
                <motion.li key={feature} variants={staggerItem} className="flex items-start gap-2.5 text-muted-foreground">
                  <span className="mt-0.5 flex size-5 shrink-0 items-center justify-center rounded-full bg-primary/12 text-primary">
                    <Check className="size-3.5" />
                  </span>
                  {feature}
                </motion.li>
              ))}
            </ul>
          </div>
        </div>
      </motion.section>
    </>
  )
}
