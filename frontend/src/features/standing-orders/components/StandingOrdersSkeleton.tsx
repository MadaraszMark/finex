import { SkeletonCard, SkeletonCardHeader } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A rendszeres átutalások váza: a havi összesítő és alatta a megbízások kártyái
export default function StandingOrdersSkeleton() {
  return (
    <>
      <SkeletonCard className="p-6 sm:p-6">
        <SkeletonCardHeader />
        <div className="mt-4 grid grid-cols-[repeat(auto-fit,minmax(min(14rem,100%),1fr))] gap-3">
          <Skeleton className="h-[4.75rem]" />
        </div>
      </SkeletonCard>
      <div className="mt-6 space-y-6">
        {[0, 1].map((index) => (
          <SkeletonCard key={index}>
            <div className="flex items-start gap-4">
              <Skeleton className="size-12 shrink-0 rounded-2xl" />
              <div className="flex-1">
                <Skeleton className="h-5 w-40 rounded-lg" />
                <Skeleton className="mt-2 h-3.5 w-64 max-w-full rounded-md" />
              </div>
              <Skeleton className="h-6 w-28 rounded-lg" />
            </div>
            <Skeleton className="mt-5 h-12" />
            <div className="mt-4 flex justify-end gap-2">
              <Skeleton className="h-9 w-28 rounded-2xl" />
              <Skeleton className="h-9 w-32 rounded-2xl" />
            </div>
          </SkeletonCard>
        ))}
      </div>
    </>
  )
}
