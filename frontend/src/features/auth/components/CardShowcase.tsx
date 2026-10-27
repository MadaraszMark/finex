import { PiggyBank, ShieldCheck, Zap } from 'lucide-react'
import { AnimatePresence, motion, useTransform } from 'motion/react'
import { useEffect, useState } from 'react'
import FinexCard from '../../../components/brand/FinexCard'
import type { PointerParallax } from '../../../hooks/usePointerParallax'
import { cn } from '../../../lib/cn'

// Váltakozó funkcióbemutató; szándékosan nincs benne kitalált tranzakció vagy összeg
const liveEvents = [
  { icon: Zap, title: 'Azonnali utalás', detail: 'FineX-es számlák között pillanatok alatt' },
  { icon: ShieldCheck, title: 'Kártyatiltás egy érintéssel', detail: 'Elveszett kártya? Azonnal letilthatod.' },
  { icon: PiggyBank, title: 'Havi kamatjóváírás', detail: 'A megtakarításaid maguktól kamatoznak' },
]

function LiveNotification() {
  const [index, setIndex] = useState(0)

  useEffect(() => {
    const timer = window.setInterval(() => setIndex((current) => (current + 1) % liveEvents.length), 3200)
    return () => window.clearInterval(timer)
  }, [])

  const event = liveEvents[index]

  return (
    <div className="absolute -top-4 right-0 w-72">
      <AnimatePresence mode="popLayout" initial={false}>
        <motion.div
          key={index}
          initial={{ opacity: 0, y: -18, scale: 0.94 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: 18, scale: 0.94 }}
          transition={{ type: 'spring', stiffness: 260, damping: 22 }}
          className="flex items-center gap-3 rounded-2xl border border-white/50 bg-white/90 p-3 pr-4 text-foreground shadow-xl backdrop-blur-xl dark:border-white/10 dark:bg-card/85"
        >
          <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300">
            <event.icon className="size-5" />
          </span>
          <div className="min-w-0 flex-1">
            <p className="text-sm font-bold">{event.title}</p>
            <p className="truncate text-xs text-muted-foreground">{event.detail}</p>
          </div>
        </motion.div>
      </AnimatePresence>
    </div>
  )
}

function SavingsGoal() {
  return (
    <motion.div
      initial={{ opacity: 0, x: 30 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: 0.7, type: 'spring', stiffness: 120, damping: 16 }}
      className="absolute right-6 bottom-0 w-60 rounded-2xl border border-white/50 bg-white/90 p-4 text-foreground shadow-xl backdrop-blur-xl dark:border-white/10 dark:bg-card/85"
    >
      <p className="flex items-center gap-2 text-sm font-bold">
        <PiggyBank className="size-4 text-primary" />
        Megtakarítási célok
      </p>
      <div className="mt-3 h-2 overflow-hidden rounded-full bg-muted">
        <motion.div
          className="h-full rounded-full bg-brand-gradient"
          initial={{ width: 0 }}
          animate={{ width: '72%' }}
          transition={{ delay: 1, duration: 1.4, ease: [0.22, 1, 0.36, 1] }}
        />
      </div>
      <p className="mt-2 text-xs text-muted-foreground">Tűzz ki célt, és kövesd a haladásod.</p>
    </motion.div>
  )
}

// Nagy képernyős "kirakat": az egérrel döntő bankkártya, személyes adatok és számok nélkül
export default function CardShowcase({ pointer, className }: { pointer: PointerParallax; className?: string }) {
  const rotateX = useTransform(pointer.y, (value) => value * -16)
  const rotateY = useTransform(pointer.x, (value) => value * 20)

  return (
    <div aria-hidden className={cn('relative h-80 w-[30rem] [perspective:1400px]', className)}>
      <motion.div
        style={{ rotateX, rotateY }}
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2, duration: 0.6 }}
        className="absolute top-2 left-20 h-52 w-80 -rotate-6 rounded-[1.75rem] border border-white/15 bg-brand-950/55 shadow-2xl backdrop-blur-md dark:border-white/10 dark:bg-brand-800/45"
      />
      <motion.div
        style={{ rotateX, rotateY }}
        initial={{ opacity: 0, y: 50, rotate: -10 }}
        animate={{ opacity: 1, y: 0, rotate: 0 }}
        transition={{ delay: 0.35, type: 'spring', stiffness: 80, damping: 14 }}
        className="absolute top-14 left-0 h-56 w-[22rem]"
      >
        <div className="size-full motion-safe:animate-float">
          <FinexCard maskedNumber="•••• •••• •••• ••••" />
        </div>
      </motion.div>
      <LiveNotification />
      <SavingsGoal />
    </div>
  )
}
