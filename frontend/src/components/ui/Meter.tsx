import { motion } from 'motion/react'
import { cn } from '../../lib/cn'

interface MeterProps {
  value: number
  max: number
  label: string
  // A felolvasó ezt mondja a nyers szám helyett (pl. "12 500 Ft a 200 000 Ft-ból")
  valueText: string
  className?: string
}

// Mérősáv egy határhoz viszonyítva; 80%-tól figyelmeztető, a határon piros
export default function Meter({ value, max, label, valueText, className }: MeterProps) {
  const ratio = max > 0 ? Math.min(value / max, 1) : 1
  const level = ratio >= 1 ? 'full' : ratio >= 0.8 ? 'high' : 'normal'

  return (
    <div
      role="meter"
      aria-label={label}
      aria-valuemin={0}
      aria-valuemax={max}
      aria-valuenow={Math.min(value, max)}
      aria-valuetext={valueText}
      className={cn('h-2.5 overflow-hidden rounded-full bg-brand-100 dark:bg-brand-950', className)}
    >
      <motion.div
        initial={{ width: 0 }}
        animate={{ width: `${ratio * 100}%` }}
        transition={{ duration: 0.9, ease: [0.22, 1, 0.36, 1] }}
        className={cn('h-full rounded-full', level === 'full' ? 'bg-danger' : level === 'high' ? 'bg-warning' : 'bg-brand-gradient')}
      />
    </div>
  )
}
