import AuroraBackground from '../../../components/brand/AuroraBackground'
import { BrandDecorations } from '../../../components/brand/Decorations'
import { SkeletonField } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A belépés és a regisztráció váza, amíg az oldal kódja első alkalommal letöltődik: a lila háttér, a fejléc,
// balra a címsorok, jobbra az üveghatású űrlapkártya – ugyanott és akkorában, mint az AuthLayout elemei
export function AuthSkeleton() {
  return (
    <div role="status" aria-busy="true" className="relative isolate flex min-h-dvh flex-col overflow-hidden">
      <span className="sr-only">Az oldal betöltése…</span>
      <AuroraBackground />
      <div aria-hidden className="flex flex-1 flex-col">
        <header className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 pt-5 sm:px-10 sm:pt-6">
          <Skeleton className="h-10 w-28 rounded-xl bg-white/15" />
          <Skeleton className="size-11 rounded-2xl bg-white/15" />
        </header>
        <main className="mx-auto grid w-full max-w-6xl flex-1 grid-cols-1 items-center gap-10 px-4 py-10 sm:px-10 lg:grid-cols-[minmax(0,1fr)_28rem] lg:gap-16">
          <section>
            <Skeleton className="h-12 w-72 max-w-full rounded-xl bg-white/20 sm:h-14 lg:h-16 lg:w-[28rem]" />
            <Skeleton className="mt-3 h-12 w-56 max-w-full rounded-xl bg-white/20 sm:h-14 lg:h-16 lg:w-80" />
            <Skeleton className="mt-6 hidden h-5 w-96 max-w-full rounded-md bg-white/15 sm:block" />
          </section>
          <div className="w-full max-w-md justify-self-center rounded-[2rem] border border-white/50 bg-card/90 p-6 shadow-[0_40px_80px_-30px_rgb(20_8_45/0.6)] backdrop-blur-2xl sm:p-9 lg:justify-self-end dark:border-white/10 dark:bg-card/80">
            <Skeleton className="h-7 w-44 rounded-lg" />
            <Skeleton className="mt-2 h-4 w-64 max-w-full rounded-md" />
            <SkeletonField className="mt-7" />
            <SkeletonField className="mt-5" />
            <div className="mt-7 flex gap-3">
              <Skeleton className="h-13 w-24 rounded-2xl" />
              <Skeleton className="h-13 flex-1 rounded-2xl" />
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}

// A nyitóképernyő váza: a márka színátmenete, középen a cím helye, alul a két gomb
export function WelcomeSkeleton() {
  return (
    <div role="status" aria-busy="true" className="relative min-h-dvh overflow-hidden bg-brand-gradient">
      <span className="sr-only">Az oldal betöltése…</span>
      <BrandDecorations />
      <div aria-hidden className="relative mx-auto flex min-h-dvh max-w-md flex-col px-6 pt-6 pb-[calc(2.5rem+env(safe-area-inset-bottom))]">
        <div className="flex justify-end">
          <Skeleton className="size-11 rounded-2xl bg-white/15" />
        </div>
        <div className="flex flex-1 flex-col items-center justify-center py-12">
          <Skeleton className="h-16 w-48 rounded-2xl bg-white/20 sm:h-20" />
          <Skeleton className="mt-6 size-16 rounded-full bg-white/15" />
          <Skeleton className="mt-6 h-5 w-64 rounded-md bg-white/15" />
          <Skeleton className="mt-2 h-5 w-48 rounded-md bg-white/15" />
        </div>
        <div className="space-y-4">
          <Skeleton className="h-14 rounded-2xl bg-white/15" />
          <div className="h-px bg-white/20" />
          <Skeleton className="h-14 rounded-2xl bg-white/15" />
        </div>
      </div>
    </div>
  )
}
