import { SkeletonCard, SkeletonCardHeader, SkeletonChips } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A Számlák oldal váza: a számlakártyák rácsa és alatta az összesítés
export function AccountsSkeleton() {
  return (
    <>
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
        {[0, 1, 2].map((index) => (
          <Skeleton key={index} className="h-48 rounded-[1.75rem]" />
        ))}
      </div>
      <SkeletonCard className="mt-6 sm:p-6">
        <SkeletonCardHeader />
        <div className="mt-5 grid grid-cols-[repeat(auto-fit,minmax(min(14rem,100%),1fr))] gap-3">
          <Skeleton className="h-[5.25rem]" />
          <Skeleton className="h-[5.25rem]" />
        </div>
      </SkeletonCard>
    </>
  )
}

// A számlaadatlap váza: az egyenleg-kártya, az időszakszűrő, a grafikon és az időszak számai, a kivonat és a lezárás
export function AccountDetailSkeleton() {
  return (
    <>
      <SkeletonCard className="p-6 sm:p-8">
        <div className="flex flex-col gap-8 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <Skeleton className="h-4 w-20 rounded-md" />
            <Skeleton className="mt-3 h-11 w-60 rounded-xl sm:h-12" />
            <Skeleton className="mt-4 h-6 w-44 rounded-full" />
          </div>
          <div className="space-y-5 lg:w-[30rem]">
            <div>
              <Skeleton className="h-4 w-32 rounded-md" />
              <Skeleton className="mt-2 h-6 w-full rounded-lg" />
            </div>
            <div>
              <Skeleton className="h-4 w-24 rounded-md" />
              <Skeleton className="mt-2 h-6 w-3/4 rounded-lg" />
            </div>
          </div>
        </div>
      </SkeletonCard>

      <div className="mt-10">
        <Skeleton className="mb-4 h-6 w-48 rounded-lg" />
        <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <SkeletonChips count={5} />
          <Skeleton className="h-11 w-72 rounded-xl" />
        </div>
        <div className="mt-5 grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)]">
          <SkeletonCard>
            <SkeletonCardHeader />
            <Skeleton className="mt-4 h-64" />
          </SkeletonCard>
          <SkeletonCard>
            <SkeletonCardHeader />
            <div className="mt-4 grid grid-cols-2 gap-3">
              {[0, 1, 2, 3].map((index) => (
                <Skeleton key={index} className="h-24" />
              ))}
            </div>
          </SkeletonCard>
        </div>
        <SkeletonCard className="mt-6">
          <SkeletonCardHeader />
          <div className="mt-4 space-y-3">
            {[0, 1, 2, 3].map((index) => (
              <Skeleton key={index} className="h-12" />
            ))}
          </div>
        </SkeletonCard>
      </div>

      <SkeletonCard className="mt-6 gap-4 p-6 sm:flex-row sm:items-center">
        <div className="flex flex-1 items-start gap-4">
          <Skeleton className="size-11 shrink-0 rounded-2xl" />
          <div className="flex-1">
            <Skeleton className="h-4 w-36 rounded-md" />
            <Skeleton className="mt-2 h-3.5 w-72 max-w-full rounded-md" />
          </div>
        </div>
        <Skeleton className="h-9 w-24 rounded-2xl" />
      </SkeletonCard>
    </>
  )
}
