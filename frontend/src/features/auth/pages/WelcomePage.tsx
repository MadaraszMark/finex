import { ArrowRight } from 'lucide-react'
import { motion } from 'motion/react'
import { BrandDecorations } from '../../../components/brand/Decorations'
import { LogoGlyph } from '../../../components/brand/Logo'
import ButtonLink from '../../../components/ui/ButtonLink'
import { ThemeToggleButton } from '../../../theme/ThemeControls'

const easeOut = [0.22, 1, 0.36, 1] as const

// Nyitóképernyő a koncepcióképek alapján: FineX felirat a pipás körrel, alatta regisztráció és belépés
export default function WelcomePage() {
  return (
    <div className="relative min-h-dvh overflow-hidden bg-brand-gradient text-white">
      <BrandDecorations />

      <div className="relative mx-auto flex min-h-dvh max-w-md flex-col px-6 pt-6 pb-[calc(2.5rem+env(safe-area-inset-bottom))]">
        <div className="flex justify-end">
          <ThemeToggleButton tone="light" />
        </div>

        <div className="flex flex-1 flex-col items-center justify-center py-12 text-center">
          <motion.h1
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, ease: easeOut }}
            className="text-6xl font-extrabold tracking-tight sm:text-7xl"
          >
            FineX
          </motion.h1>
          <motion.div
            initial={{ opacity: 0, scale: 0.6 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.25, type: 'spring', bounce: 0.45, duration: 0.7 }}
          >
            <LogoGlyph className="mt-6 size-16" strokeWidth={2.5} />
          </motion.div>
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.4, duration: 0.6 }}
            className="mt-6 max-w-xs text-lg text-white/85"
          >
            A pénzügyeid egy helyen – gyorsan, átláthatóan, biztonságosan.
          </motion.p>
        </div>

        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.35, duration: 0.6, ease: easeOut }}
          className="space-y-4"
        >
          <ButtonLink to="/register" variant="deep" size="lg" fullWidth rightIcon={<ArrowRight />} className="justify-between">
            Regisztráció
          </ButtonLink>
          <div aria-hidden className="h-px bg-white/20" />
          <ButtonLink to="/login" variant="glass" size="lg" fullWidth rightIcon={<ArrowRight />} className="justify-between">
            Bejelentkezés
          </ButtonLink>
        </motion.div>
      </div>
    </div>
  )
}
