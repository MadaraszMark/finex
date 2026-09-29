import { cn } from '../../lib/cn'

// A koncepcióképek díszítőelemei: hullámvonalak, pöttyrács és áttetsző körök a lila háttéren

export function Waves({ className, strokeWidth = 4 }: { className?: string; strokeWidth?: number }) {
  return (
    <svg viewBox="0 0 120 64" fill="none" aria-hidden className={className}>
      <path d="M6 20c12-12 24-12 36 0s24 12 36 0 24-12 36 0" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" />
      <path d="M6 44c12-12 24-12 36 0s24 12 36 0 24-12 36 0" stroke="currentColor" strokeWidth={strokeWidth} strokeLinecap="round" />
    </svg>
  )
}

export function DotGrid({ className, rows = 5, columns = 5 }: { className?: string; rows?: number; columns?: number }) {
  const gap = 12
  return (
    <svg viewBox={`0 0 ${columns * gap} ${rows * gap}`} fill="currentColor" aria-hidden className={className}>
      {Array.from({ length: rows * columns }, (_, index) => (
        <circle key={index} cx={(index % columns) * gap + gap / 2} cy={Math.floor(index / columns) * gap + gap / 2} r={1.6} />
      ))}
    </svg>
  )
}

// A nyitóképernyő lila hátterének teljes díszítése egyben
export function BrandDecorations({ className }: { className?: string }) {
  return (
    <div aria-hidden className={cn('pointer-events-none absolute inset-0 overflow-hidden', className)}>
      <Waves className="absolute top-8 left-6 w-12 text-white/50" />
      <Waves className="absolute top-16 -right-8 w-44 animate-float text-brand-200/55" strokeWidth={11} />
      <div className="absolute top-[38%] -left-24 size-60 rounded-full bg-brand-800/45" />
      <div className="absolute top-28 left-[38%] size-24 animate-float rounded-full bg-white/10 [animation-delay:-4s]" />
      <DotGrid className="absolute top-[24%] right-6 w-24 text-white/45" />
      <div className="absolute -bottom-12 left-10 size-36 rounded-full bg-white/10" />
      <Waves className="absolute right-4 bottom-28 w-16 text-white/40" />
    </div>
  )
}
