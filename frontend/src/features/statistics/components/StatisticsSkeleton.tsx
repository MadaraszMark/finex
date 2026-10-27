import { SkeletonCard, SkeletonCardHeader, SkeletonChips, SkeletonRows, SkeletonStatTile } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A kategóriák rangsorának sorai
export function CategoryRowsSkeleton({ count }: { count: number }) {
  return (
    <div className="space-y-4">
      {Array.from({ length: count }, (_, index) => (
        <div key={index}>
          <div className="flex items-center gap-3">
            <Skeleton className="size-10 shrink-0 rounded-xl" />
            <div className="flex-1">
              <Skeleton className="h-4 w-28 rounded-md" />
              <Skeleton className="mt-1.5 h-3 w-12 rounded-md" />
            </div>
            <Skeleton className="h-4 w-20 rounded-md" />
          </div>
          <Skeleton className="mt-2 ml-13 h-2 rounded-full" />
        </div>
      ))}
    </div>
  )
}

// A Statisztika oldal váza: szűrők, mutatók, grafikon és két kártya
export default function StatisticsSkeleton() {
  return (
    <>
      <SkeletonCard className="p-4 sm:p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SkeletonChips count={4} />
          <Skeleton className="h-10 w-36 rounded-2xl" />
        </div>
      </SkeletonCard>
      <div className="mt-6 grid grid-cols-2 gap-3 lg:grid-cols-4 lg:gap-6">
        {[0, 1, 2, 3].map((index) => (
          <SkeletonStatTile key={index} />
        ))}
      </div>
      <SkeletonCard className="mt-6">
        <SkeletonCardHeader action />
        <Skeleton className="mt-4 h-4 w-40 rounded-md" />
        <Skeleton className="mt-4 h-60" />
      </SkeletonCard>
      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <SkeletonCard>
          <SkeletonCardHeader />
          <div className="mt-5">
            <CategoryRowsSkeleton count={6} />
          </div>
        </SkeletonCard>
        <SkeletonCard>
          <SkeletonCardHeader />
          <SkeletonRows count={6} className="mt-2" />
        </SkeletonCard>
      </div>
    </>
  )
}
