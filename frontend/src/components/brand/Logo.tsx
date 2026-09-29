import { useId } from 'react'
import { cn } from '../../lib/cn'

// A koncepcióképek jele: pipa egy körben
export function LogoGlyph({ className, strokeWidth = 3 }: { className?: string; strokeWidth?: number }) {
  return (
    <svg viewBox="0 0 48 48" fill="none" aria-hidden className={className}>
      <circle cx="24" cy="24" r="17" stroke="currentColor" strokeWidth={strokeWidth} />
      <path d="M16.5 24.5l5 5 10-10.5" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

type LogoTone = 'default' | 'light'
type LogoSize = 'sm' | 'md' | 'lg'

const markSizes: Record<LogoSize, string> = {
  sm: 'size-8 rounded-[0.7rem]',
  md: 'size-10 rounded-xl',
  lg: 'size-12 rounded-2xl',
}

const wordmarkSizes: Record<LogoSize, string> = {
  sm: 'text-lg',
  md: 'text-xl',
  lg: 'text-2xl',
}

// Az alkalmazás ikonja (a favicon is ez): lila színátmenetes, lekerekített négyzet, benne a pipás kör
export function LogoMark({ tone = 'default', size = 'md', className }: { tone?: LogoTone; size?: LogoSize; className?: string }) {
  const gradientId = useId()

  if (tone === 'light') {
    return (
      <span className={cn('flex items-center justify-center border border-white/35 bg-white/15 text-white backdrop-blur-md', markSizes[size], className)}>
        <LogoGlyph className="size-[70%]" />
      </span>
    )
  }

  return (
    <svg viewBox="0 0 48 48" aria-hidden className={cn('shrink-0 shadow-glow', markSizes[size], className)}>
      <defs>
        <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#b58af7" />
          <stop offset="0.5" stopColor="#8a4fe0" />
          <stop offset="1" stopColor="#5f33b5" />
        </linearGradient>
      </defs>
      <rect width="48" height="48" rx="14" fill={`url(#${gradientId})`} />
      <circle cx="24" cy="24" r="11.5" fill="none" stroke="#fff" strokeWidth="3" />
      <path d="M19 24.5l3.5 3.5 6.5-7" fill="none" stroke="#fff" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  )
}

export default function Logo({ tone = 'default', size = 'md', className }: { tone?: LogoTone; size?: LogoSize; className?: string }) {
  return (
    <span className={cn('inline-flex items-center gap-2.5', className)}>
      <LogoMark tone={tone} size={size} />
      <span className={cn('font-extrabold tracking-tight', wordmarkSizes[size], tone === 'light' ? 'text-white' : 'text-foreground')}>FineX</span>
    </span>
  )
}
