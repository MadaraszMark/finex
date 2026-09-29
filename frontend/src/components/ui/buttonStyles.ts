import { cn } from '../../lib/cn'

// A gombok stílusa külön függvényben van, így ugyanúgy néz ki a <button> és a navigáló <Link> is

export type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger' | 'glass' | 'deep'
export type ButtonSize = 'sm' | 'md' | 'lg'

export interface ButtonStyleOptions {
  variant?: ButtonVariant
  size?: ButtonSize
  fullWidth?: boolean
  className?: string
}

const base =
  'group inline-flex items-center justify-center gap-2 rounded-2xl font-semibold whitespace-nowrap select-none transition duration-200 ' +
  'focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-ring/40 active:scale-[0.98] ' +
  'disabled:pointer-events-none disabled:opacity-60 [&_svg]:size-5 [&_svg]:shrink-0'

// Egérrel fölé állva fényvillanás fut végig a gombon
const shine =
  'relative overflow-hidden before:pointer-events-none before:absolute before:inset-y-0 before:left-0 before:w-1/3 ' +
  'before:-translate-x-full before:skew-x-[-20deg] before:bg-white/25 before:transition-transform before:duration-700 ' +
  'hover:before:translate-x-[400%]'

const variants: Record<ButtonVariant, string> = {
  // A koncepcióképek "Belépés" gombja: lila színátmenet, lila fényű árnyékkal
  primary: `bg-brand-gradient text-white shadow-glow hover:-translate-y-px hover:brightness-110 ${shine}`,
  secondary: 'bg-muted text-foreground hover:bg-brand-100 dark:hover:bg-brand-950',
  outline: 'border border-border bg-card text-foreground hover:border-brand-300 hover:text-primary',
  ghost: 'text-muted-foreground hover:bg-muted hover:text-foreground',
  danger: 'bg-danger text-white hover:brightness-110',
  // Lila háttéren: áttetsző, üveghatású (nyitóképernyő "Bejelentkezés")
  glass: 'border border-white/45 bg-white/15 text-white backdrop-blur-md hover:bg-white/25',
  // Lila háttéren: sötét ibolya (nyitóképernyő "Regisztráció")
  deep: 'bg-brand-950/60 text-white backdrop-blur-md hover:bg-brand-950/75',
}

const sizes: Record<ButtonSize, string> = {
  sm: 'h-9 px-3.5 text-sm',
  md: 'h-11 px-5 text-sm',
  lg: 'h-14 px-6 text-base',
}

export function buttonStyles({ variant = 'primary', size = 'md', fullWidth = false, className }: ButtonStyleOptions = {}): string {
  return cn(base, variants[variant], sizes[size], fullWidth && 'w-full', className)
}

export type IconButtonTone = 'default' | 'light'

// Kerek sarkú, négyzetes ikongomb (pl. a fejléc csengője); a "light" változat lila háttérre való
export function iconButtonStyles(tone: IconButtonTone = 'default', className?: string): string {
  return cn(
    'relative inline-flex size-11 shrink-0 items-center justify-center rounded-2xl transition duration-200 ' +
      'focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-ring/40 active:scale-95 [&_svg]:size-5',
    tone === 'default'
      ? 'border border-border bg-card text-muted-foreground hover:border-brand-300 hover:text-foreground'
      : 'border border-white/35 bg-white/15 text-white backdrop-blur-md hover:bg-white/25',
    className,
  )
}
