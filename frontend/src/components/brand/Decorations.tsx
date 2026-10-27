// Díszítőelemek a lila háttérre: hullámvonalak és pöttyrács

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
