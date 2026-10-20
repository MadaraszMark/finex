import type { ReactNode } from 'react'
import { cn } from '../../lib/cn'
import AuroraBackground from '../brand/AuroraBackground'
import { cardSurface } from '../ui/cardStyles'
import Skeleton from '../ui/Skeleton'

// Betöltéskori vázak a valódi elemek helyén és méretében: kártya, kártyafejléc, listasorok, mutatócsempe és az
// oldalfejléc a lila sávon. Az oldalankénti vázak (features/*/components/…Skeleton) ezekből épülnek fel, így az adatok
// megérkezésekor a tartalom pontosan a váz helyére kerül, semmi sem ugrik (pörgő töltésjelző helyett).

export function SkeletonCard({ className, children }: { className?: string; children?: ReactNode }) {
  return <div className={cn(cardSurface, 'flex flex-col p-5 sm:p-6', className)}>{children}</div>
}

// A kártyafejléc (cím és leírás), jobbra opcionálisan egy művelet (pl. "Részletek →")
export function SkeletonCardHeader({ action = false, description = true, className }: { action?: boolean; description?: boolean; className?: string }) {
  return (
    <div className={cn('flex items-start justify-between gap-4', className)}>
      <div className="min-w-0 flex-1">
        <Skeleton className="h-5 w-40 max-w-full rounded-lg" />
        {description && <Skeleton className="mt-2 h-3.5 w-56 max-w-full rounded-md" />}
      </div>
      {action && <Skeleton className="h-4 w-20 shrink-0 rounded-md" />}
    </div>
  )
}

// Listasorok (pl. tranzakciók, belépések): ikon, kétsoros szöveg, jobbra összeg és dátum
export function SkeletonRows({ count, className }: { count: number; className?: string }) {
  return (
    <div className={cn('divide-y divide-border', className)}>
      {Array.from({ length: count }, (_, index) => (
        <div key={index} className="flex items-center gap-3.5 py-3">
          <Skeleton className="size-11 shrink-0 rounded-2xl" />
          <div className="min-w-0 flex-1">
            <Skeleton className="h-4 w-2/5 rounded-md" />
            <Skeleton className="mt-2 h-3 w-1/4 rounded-md" />
          </div>
          <div className="flex shrink-0 flex-col items-end">
            <Skeleton className="h-4 w-20 rounded-md" />
            <Skeleton className="mt-2 h-3 w-12 rounded-md" />
          </div>
        </div>
      ))}
    </div>
  )
}

// Mutatócsempe: címke, érték és opcionálisan egy magyarázó sor
export function SkeletonStatTile({ meta = false, className }: { meta?: boolean; className?: string }) {
  return (
    <div className={cn(cardSurface, 'flex flex-col p-4 sm:p-5', className)}>
      <Skeleton className="h-4 w-28 max-w-full rounded-md" />
      <Skeleton className="mt-3 h-7 w-3/5 rounded-lg" />
      {meta && <Skeleton className="mt-3 h-3.5 w-2/5 rounded-md" />}
    </div>
  )
}

// Címkés beviteli mező (címke és a mező)
export function SkeletonField({ className }: { className?: string }) {
  return (
    <div className={className}>
      <Skeleton className="h-4 w-24 rounded-md" />
      <Skeleton className="mt-2 h-13 w-full" />
    </div>
  )
}

// Pirulasor (szűrők, időszakválasztó)
export function SkeletonChips({ count, className }: { count: number; className?: string }) {
  return (
    <div className={cn('flex flex-wrap gap-2', className)}>
      {Array.from({ length: count }, (_, index) => (
        <Skeleton key={index} className={cn('h-9 rounded-full', index % 3 === 0 ? 'w-20' : index % 3 === 1 ? 'w-24' : 'w-28')} />
      ))}
    </div>
  )
}

export interface PageHeaderSkeletonProps {
  eyebrow?: boolean
  description?: boolean
  action?: boolean
}

// Az oldalfejléc váza a lila sávon, a PageHeader méreteivel: felirat, cím, leírás és jobbra a művelet gombja
export function PageHeaderSkeleton({ eyebrow = false, description = true, action = false }: PageHeaderSkeletonProps) {
  return (
    <div className="relative mb-8 lg:mb-10">
      <div className="pointer-events-none absolute -top-40 -bottom-32 left-[calc(50%-50vw)] isolate -z-10 w-screen mask-fade-bottom">
        <AuroraBackground variant="band" />
      </div>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="min-w-0 flex-1">
          {eyebrow && <Skeleton className="mb-3 h-3.5 w-40 rounded-md bg-white/15" />}
          <Skeleton className="h-9 w-64 max-w-full rounded-xl bg-white/20 sm:h-10 lg:h-12 lg:w-80" />
          {description && <Skeleton className="mt-4 h-4 w-80 max-w-full rounded-md bg-white/15" />}
        </div>
        {action && <Skeleton className="h-11 w-40 shrink-0 rounded-2xl bg-white/15" />}
      </div>
    </div>
  )
}

// Egy teljes oldal váza (fejléc + tartalom), amíg az oldal kódja első betöltéskor letöltődik; a felolvasóprogram
// egyszer bemondja, hogy az oldal töltődik
export function PageSkeleton({ header, children }: { header?: PageHeaderSkeletonProps; children: ReactNode }) {
  return (
    <div role="status" aria-busy="true">
      <span className="sr-only">Az oldal betöltése…</span>
      <div aria-hidden>
        <PageHeaderSkeleton {...header} />
        {children}
      </div>
    </div>
  )
}
