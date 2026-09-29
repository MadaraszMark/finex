import { useLayoutEffect, useRef } from 'react'
import { cn } from '../../lib/cn'

type AuroraVariant = 'screen' | 'band'

// A fényfoltok elhelyezése: teljes képernyőn (belépő oldalak) a világos folt alul "derengés", az alacsony, széles
// fejlécsávban viszont fehéres párának hatna, ezért ott mélyebb lila foltok úsznak az alsó részen
const blobs: Record<AuroraVariant, string[]> = {
  screen: [
    '-top-48 -left-40 size-[38rem] bg-fuchsia-400/45 motion-safe:animate-aurora-1 dark:bg-brand-600/55',
    'top-1/4 -right-48 size-[42rem] bg-indigo-500/40 motion-safe:animate-aurora-2 dark:bg-indigo-600/35',
    '-bottom-56 left-1/4 size-[34rem] bg-violet-300/40 motion-safe:animate-aurora-3 dark:bg-fuchsia-600/30',
  ],
  band: [
    '-top-64 -left-40 size-[38rem] bg-fuchsia-400/40 motion-safe:animate-aurora-1 dark:bg-brand-600/55',
    '-top-40 -right-40 size-[40rem] bg-indigo-500/40 motion-safe:animate-aurora-2 dark:bg-indigo-600/35',
    '-bottom-72 left-1/3 size-[36rem] bg-brand-600/35 motion-safe:animate-aurora-3 dark:bg-fuchsia-600/25',
  ],
}

// A belépő oldalak és az oldalfejlécek animált háttere: a márka lila színátmenete, rajta lassan úszó, elmosott
// fényfoltok ("aurora") és egy finom pöttyháló. Világos módban élénk lila, sötét módban mély ibolya.
export default function AuroraBackground({ variant = 'screen' }: { variant?: AuroraVariant }) {
  const rootRef = useRef<HTMLDivElement>(null)

  // A foltok animációja az oldal betöltésének pillanatához igazodik, nem a saját megjelenéséhez: oldalváltáskor
  // (amikor új fejléc, és vele új háttér jön létre) így ott folytatják az úszást, ahol az előző oldalon tartottak
  useLayoutEffect(() => {
    for (const animation of rootRef.current?.getAnimations({ subtree: true }) ?? []) {
      animation.startTime = 0
    }
  }, [])

  return (
    <div ref={rootRef} aria-hidden className="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
      <div className="absolute inset-0 bg-[linear-gradient(135deg,#a878f5_0%,#8a4fe0_45%,#5f33b5_100%)] dark:bg-[linear-gradient(135deg,#231440_0%,#150c2a_50%,#0d0a16_100%)]" />

      {blobs[variant].map((blob) => (
        <div key={blob} className={cn('absolute rounded-full blur-3xl will-change-transform', blob)} />
      ))}

      <div className="absolute inset-0 bg-[radial-gradient(rgb(255_255_255/0.16)_1px,transparent_1px)] [mask-image:radial-gradient(ellipse_at_center,black_20%,transparent_75%)] bg-[length:28px_28px]" />
    </div>
  )
}
