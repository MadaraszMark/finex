import { SkeletonCard, SkeletonCardHeader, SkeletonRows, SkeletonStatTile } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A főoldal váza a valódi 3 oszlopos rácson
export default function HomeSkeleton() {
  return (
    <>
      <div className="-mx-4 flex gap-4 overflow-hidden px-4 pt-1 pb-6 sm:-mx-6 sm:px-6 lg:mx-0 lg:grid lg:grid-cols-3 lg:gap-6 lg:px-0">
        {[0, 1, 2].map((index) => (
          <Skeleton key={index} className="h-48 w-[min(20rem,82vw)] shrink-0 rounded-[1.75rem] lg:w-auto" />
        ))}
      </div>

      <div className="mt-10">
        <div className="mb-4 flex items-center justify-between gap-3">
          <Skeleton className="h-6 w-28 rounded-lg" />
          <Skeleton className="h-10 w-36 rounded-2xl" />
        </div>
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-3 lg:gap-6">
          <SkeletonStatTile meta className="col-span-2 sm:p-6 lg:col-span-1" />
          <SkeletonStatTile meta className="sm:p-6" />
          <SkeletonStatTile meta className="sm:p-6" />
        </div>
        <div className="mt-4 grid grid-cols-1 gap-4 lg:mt-6 lg:grid-cols-3 lg:gap-6">
          <SkeletonCard className="lg:col-span-2">
            <SkeletonCardHeader action />
            <Skeleton className="mt-4 h-4 w-40 rounded-md" />
            <Skeleton className="mt-4 h-60" />
          </SkeletonCard>
          <SkeletonCard>
            <SkeletonCardHeader action description={false} />
            <Skeleton className="mt-4 h-7 w-36 rounded-lg" />
            <Skeleton className="mt-2 h-3.5 w-28 rounded-md" />
            <div className="mt-6 space-y-6">
              {[0, 1, 2].map((index) => (
                <div key={index}>
                  <div className="flex justify-between gap-3">
                    <Skeleton className="h-4 w-24 rounded-md" />
                    <Skeleton className="h-4 w-20 rounded-md" />
                  </div>
                  <Skeleton className="mt-2 h-2 w-full rounded-full" />
                </div>
              ))}
            </div>
          </SkeletonCard>
        </div>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 lg:mt-6 lg:grid-cols-3 lg:gap-6">
        <SkeletonCard className="lg:col-span-2">
          <SkeletonCardHeader action description={false} />
          <SkeletonRows count={6} className="mt-2" />
        </SkeletonCard>
        <SkeletonCard>
          <SkeletonCardHeader action />
          <div className="flex flex-1 items-center justify-center py-7">
            <Skeleton className="h-56 w-full max-w-[22rem] rounded-[1.75rem]" />
          </div>
          <div className="flex justify-between gap-3">
            <Skeleton className="h-4 w-20 rounded-md" />
            <Skeleton className="h-4 w-32 rounded-md" />
          </div>
          <Skeleton className="mt-2.5 h-2.5 w-full rounded-full" />
        </SkeletonCard>
      </div>
    </>
  )
}
