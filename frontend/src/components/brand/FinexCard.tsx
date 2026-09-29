import { Nfc } from 'lucide-react'
import { cn } from '../../lib/cn'

interface FinexCardProps {
  holderName?: string
  maskedNumber?: string
  expiry?: string
  // "glass": lila háttérre (belépő oldal), "solid": bármilyen háttérre (pl. a főoldal világos felületén is)
  variant?: 'glass' | 'solid'
  className?: string
}

const surfaces: Record<NonNullable<FinexCardProps['variant']>, string> = {
  // Sötét módban az üveg szürkének hatna, ezért ott ez is lila színátmenetes
  glass:
    'border-white/35 bg-[linear-gradient(135deg,rgb(255_255_255/0.34),rgb(255_255_255/0.08))] backdrop-blur-xl ' +
    'dark:border-white/20 dark:bg-[linear-gradient(135deg,#a878f5_0%,#7c45d1_45%,#3b1d7a_100%)]',
  solid: 'border-white/20 bg-[linear-gradient(135deg,#a878f5_0%,#7c45d1_45%,#3b1d7a_100%)]',
}

// A FineX bankkártya grafikája (belépő oldal, főoldal); időnként fényvillanás fut végig rajta
export default function FinexCard({
  holderName = 'Kovács Anna',
  maskedNumber = '•••• •••• •••• 1184',
  expiry = '09/30',
  variant = 'glass',
  className,
}: FinexCardProps) {
  return (
    <div
      className={cn(
        '@container relative size-full overflow-hidden rounded-[1.75rem] border p-6 text-white shadow-[0_30px_60px_-24px_rgb(20_8_45/0.75)]',
        surfaces[variant],
        className,
      )}
    >
      <div aria-hidden className="absolute inset-y-0 left-0 w-1/3 bg-linear-to-r from-transparent via-white/40 to-transparent motion-safe:animate-shine" />

      <div className="relative flex items-start justify-between">
        <span className="text-xl font-extrabold tracking-tight">FineX</span>
        <Nfc className="size-6 text-white/80" />
      </div>
      <div className="relative mt-5 h-9 w-12 rounded-lg bg-[linear-gradient(135deg,#fde68a,#f59e0b)] opacity-90 shadow-inner" />
      {/* A kártyaszám mindig egy sorban marad: keskeny kártyán (kis telefon) arányosan kisebb a betű */}
      <p className="relative mt-5 font-mono text-[length:min(1.25rem,calc(100cqw/15))] tracking-[0.18em] whitespace-nowrap">{maskedNumber}</p>
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
    </div>
  )
}
