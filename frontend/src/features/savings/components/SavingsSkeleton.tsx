import { SkeletonCard, SkeletonCardHeader } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A megtakarítások váza: a célok kártyái és az összesítés
export default function SavingsSkeleton() {
  return (
    <>
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 2xl:grid-cols-3">
        {[0, 1].map((index) => (
          <SkeletonCard key={index}>
            <div className="flex items-start justify-between gap-3">
              <div className="flex-1">
                <Skeleton className="h-5 w-32 rounded-lg" />
                <Skeleton className="mt-2 h-3.5 w-56 max-w-full rounded-md" />
              </div>
              <Skeleton className="size-9 rounded-2xl" />
            </div>
            <div className="mt-6 flex items-center gap-5">
              <Skeleton className="size-28 shrink-0 rounded-full" />
              <div className="flex-1">
                <Skeleton className="h-8 w-36 rounded-lg" />
                <Skeleton className="mt-2 h-3.5 w-28 rounded-md" />
                <Skeleton className="mt-2 h-3.5 w-40 rounded-md" />
              </div>
            </div>
            <Skeleton className="mt-5 h-11" />
            <div className="mt-auto flex gap-2 pt-5">
              <Skeleton className="h-9 flex-1 rounded-2xl" />
              <Skeleton className="h-9 flex-1 rounded-2xl" />
            </div>
          </SkeletonCard>
        ))}
      </div>
      <SkeletonCard className="mt-6 p-6 sm:p-6">
        <SkeletonCardHeader />
        <div className="mt-5 grid grid-cols-[repeat(auto-fit,minmax(min(14rem,100%),1fr))] gap-3">
          <Skeleton className="h-[6.5rem]" />
        </div>
      </SkeletonCard>
    </>
  )
}
