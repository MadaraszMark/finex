import type { ReactNode } from 'react'
import { cn } from '../../lib/cn'

export type BadgeTone = 'brand' | 'neutral' | 'success' | 'danger' | 'warning'

const tones: Record<BadgeTone, string> = {
  brand: 'bg-brand-100 text-brand-700 dark:bg-brand-950 dark:text-brand-300',
  neutral: 'bg-muted text-muted-foreground',
  success: 'bg-success/12 text-success',
  danger: 'bg-danger/12 text-danger',
  warning: 'bg-warning/15 text-warning',
}

export default function Badge({ tone = 'neutral', className, children }: { tone?: BadgeTone; className?: string; children: ReactNode }) {
  return (
    <span className={cn('inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold', tones[tone], className)}>
      {children}
    </span>
  )
}
