import { ArrowLeft, Headset, ShieldX } from 'lucide-react'
import { motion } from 'motion/react'
import ButtonLink from '../../components/ui/ButtonLink'
import { cardSurface } from '../../components/ui/cardStyles'
import PageHeader from '../../components/ui/PageHeader'
import { cn } from '../../lib/cn'
import { cardReveal, staggerItem } from '../../lib/motion'

// Admin oldal USER szerepkörrel (a backend is 403-mal utasítaná el a kéréseket)
export default function ForbiddenPage() {
  return (
    <>
      <PageHeader eyebrow="403-as hiba" title="Ehhez nincs jogosultságod" description="Ez az oldal csak az adminisztrátorok számára érhető el." />

      <motion.section variants={cardReveal} initial="hidden" animate="show" className={cn(cardSurface, 'px-6 py-10 text-center sm:px-10 sm:py-12')}>
        <motion.span
          variants={staggerItem}
          className="relative mx-auto flex size-20 items-center justify-center rounded-[1.75rem] bg-brand-gradient text-white shadow-glow"
        >
          <span aria-hidden className="absolute inset-0 rounded-[1.75rem] ring-2 ring-brand-400/70 motion-safe:animate-pulse-ring" />
          <ShieldX className="size-9" />
        </motion.span>
        <motion.p variants={staggerItem} className="mx-auto mt-6 max-w-md text-muted-foreground">
          Ha szerinted ez tévedés, írj az ügyfélszolgálatnak, és megnézzük a jogosultságaidat.
        </motion.p>
        <motion.div variants={staggerItem} className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          <ButtonLink to="/" leftIcon={<ArrowLeft />}>
            Vissza a főoldalra
          </ButtonLink>
          <ButtonLink to="/support" variant="outline" leftIcon={<Headset />}>
            Ügyfélszolgálat
          </ButtonLink>
        </motion.div>
      </motion.section>
    </>
  )
}
