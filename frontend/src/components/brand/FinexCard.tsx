import { Lock, Nfc } from 'lucide-react'
import type { CardStatus } from '../../api/types'
import { cn } from '../../lib/cn'

interface FinexCardProps {
  // Ha hiányoznak, a kártya nem mutat kitalált nevet vagy számot, csak a márkát és a chipet
  holderName?: string
  maskedNumber?: string
  expiry?: string
  // glass: lila háttérre, solid: bármilyen háttérre
  variant?: 'glass' | 'solid'
  status?: CardStatus
  className?: string
}

const surfaces: Record<NonNullable<FinexCardProps['variant']>, string> = {
  // Sötét módban az üveg szürkének hatna, ezért ott ez is lila színátmenetes
  glass:
    'border-white/35 bg-[linear-gradient(135deg,rgb(255_255_255/0.34),rgb(255_255_255/0.08))] backdrop-blur-xl ' +
    'dark:border-white/20 dark:bg-[linear-gradient(135deg,#a878f5_0%,#7c45d1_45%,#3b1d7a_100%)]',
  solid: 'border-white/20 bg-[linear-gradient(135deg,#a878f5_0%,#7c45d1_45%,#3b1d7a_100%)]',
}

const statusLabels: Partial<Record<CardStatus, string>> = {
  BLOCKED: 'Letiltva',
  CANCELLED: 'Megszűnt',
}

// A FineX bankkártya grafikája, időnként végigfutó fényvillanással
export default function FinexCard({
  holderName,
  maskedNumber,
  expiry,
  variant = 'glass',
  status,
  className,
}: FinexCardProps) {
  const statusLabel = status ? statusLabels[status] : undefined

  return (
    <div
      className={cn(
        '@container relative size-full overflow-hidden rounded-[1.75rem] border p-6 text-white shadow-[0_30px_60px_-24px_rgb(20_8_45/0.75)]',
        surfaces[variant],
        className,
        status === 'CANCELLED' && 'grayscale',
      )}
    >
      <div aria-hidden className="absolute inset-y-0 left-0 w-1/3 bg-linear-to-r from-transparent via-white/40 to-transparent motion-safe:animate-shine" />

      <div className="relative flex items-start justify-between">
        <span className="text-xl font-extrabold tracking-tight">FineX</span>
        <Nfc className="size-6 text-white/80" />
      </div>
      <div className="relative mt-5 h-9 w-12 rounded-lg bg-[linear-gradient(135deg,#fde68a,#f59e0b)] opacity-90 shadow-inner" />
      {/* Keskeny kártyán arányosan kisebb a betű, hogy a szám egy sorban maradjon */}
      {maskedNumber && (
        <p className="relative mt-5 font-mono text-[length:min(1.25rem,calc(100cqw/15))] tracking-[0.18em] whitespace-nowrap">{maskedNumber}</p>
      )}
      {(holderName || expiry) && (
        <div className="relative mt-4 flex items-end justify-between gap-4 text-[11px] font-semibold tracking-widest text-white/80 uppercase">
          <div className="min-w-0">
            <p className="text-[9px] text-white/55">Kártyabirtokos</p>
            <p className="truncate">{holderName}</p>
          </div>
          <div className="shrink-0 text-right">
            <p className="text-[9px] text-white/55">Lejárat</p>
            <p>{expiry}</p>
          </div>
        </div>
      )}

      {statusLabel && (
        <div className="absolute inset-0 flex items-center justify-center bg-brand-950/45 backdrop-blur-[2px]">
          <span className="inline-flex items-center gap-1.5 rounded-full bg-white/90 px-3.5 py-1.5 text-sm font-bold text-brand-950 shadow-lg">
            <Lock aria-hidden className="size-4" />
            {statusLabel}
          </span>
        </div>
      )}
    </div>
  )
}
