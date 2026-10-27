import AuroraBackground from '../../../components/brand/AuroraBackground'
import { SkeletonField } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A belépés és a regisztráció váza, amíg az oldal kódja első alkalommal letöltődik
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

// A nyitóképernyő váza (ugyanaz az elrendezés, mint a WelcomePage-en)
export function WelcomeSkeleton() {
  return (
    <div role="status" aria-busy="true" className="relative isolate flex min-h-dvh flex-col overflow-hidden">
      <span className="sr-only">Az oldal betöltése…</span>
      <AuroraBackground />
      <div aria-hidden className="flex flex-1 flex-col">
        <header className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 pt-5 sm:px-10 sm:pt-6">
          <Skeleton className="h-10 w-28 rounded-xl bg-white/15" />
          <Skeleton className="size-11 rounded-2xl bg-white/15" />
        </header>
        <main className="mx-auto grid w-full max-w-6xl flex-1 grid-cols-1 gap-10 px-4 pt-8 pb-8 sm:items-center sm:px-10 sm:py-10 xl:grid-cols-[minmax(0,1fr)_30rem] xl:gap-16">
          <section className="flex flex-col">
            <div className="flex flex-1 flex-col items-center justify-center sm:flex-none xl:items-start">
              <Skeleton className="h-9 w-40 rounded-full bg-white/15" />
              <Skeleton className="mt-6 h-10 w-64 max-w-full rounded-xl bg-white/20 min-[400px]:h-12 sm:h-16 sm:w-96 xl:h-[4.75rem] xl:w-[28rem]" />
              <Skeleton className="mt-2 h-10 w-48 max-w-full rounded-xl bg-white/20 min-[400px]:h-12 sm:h-16 sm:w-72 xl:h-[4.75rem] xl:w-80" />
              <Skeleton className="mt-5 h-5 w-80 max-w-full rounded-md bg-white/15" />
              <Skeleton className="mt-2 h-5 w-56 max-w-full rounded-md bg-white/15" />
            </div>
            <div className="mt-10 flex flex-col gap-4 sm:flex-row sm:justify-center xl:justify-start">
              <Skeleton className="h-14 rounded-2xl bg-white/15 sm:w-60" />
              <div className="h-px bg-white/20 sm:hidden" />
              <Skeleton className="h-14 rounded-2xl bg-white/15 sm:w-60" />
            </div>
          </section>
          <div className="hidden h-80 w-[30rem] justify-self-end xl:block">
            <Skeleton className="mt-14 h-56 w-[22rem] rounded-[1.75rem] bg-white/15" />
          </div>
        </main>
        <div className="mx-auto hidden w-full max-w-6xl grid-cols-2 gap-4 px-4 pb-8 sm:grid sm:px-10 lg:grid-cols-4 lg:gap-6">
          {[0, 1, 2, 3].map((index) => (
            <Skeleton key={index} className="h-44 rounded-3xl bg-white/10" />
          ))}
        </div>
      </div>
    </div>
  )
}
