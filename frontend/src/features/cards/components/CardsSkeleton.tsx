import { SkeletonCard } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A Kártyák oldal váza: balra a kártyák, jobbra a beállítások
export default function CardsSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,22rem)_minmax(0,1fr)] lg:items-start">
      <div className="-mx-4 flex gap-4 overflow-hidden px-4 pb-6 sm:-mx-6 sm:px-6 lg:mx-0 lg:flex-col lg:gap-5 lg:px-0 lg:pb-0">
        {[0, 1].map((index) => (
          <div key={index} className="w-[min(20rem,82vw)] shrink-0 p-1.5 lg:w-full">
            <Skeleton className="h-52 rounded-[1.75rem]" />
            <div className="flex justify-between gap-3 px-3 pt-3 pb-1.5">
              <Skeleton className="h-4 w-24 rounded-md" />
              <Skeleton className="h-4 w-10 rounded-md" />
            </div>
          </div>
        ))}
      </div>

      <SkeletonCard className="p-6 sm:p-8">
        <div className="flex items-start justify-between gap-4">
          <div>
            <Skeleton className="h-6 w-36 rounded-lg" />
            <Skeleton className="mt-2 h-4 w-44 rounded-md" />
          </div>
          <Skeleton className="h-6 w-16 rounded-full" />
        </div>
        <Skeleton className="mt-6 h-24" />
        <div className="mt-6 space-y-5">
          {[0, 1].map((index) => (
            <div key={index} className="flex items-center justify-between gap-4">
              <div className="flex-1">
                <Skeleton className="h-4 w-32 rounded-md" />
                <Skeleton className="mt-2 h-3.5 w-64 max-w-full rounded-md" />
              </div>
              <Skeleton className="h-8 w-14 rounded-full" />
            </div>
          ))}
        </div>
        <div className="mt-8">
          <div className="flex justify-between gap-4">
            <Skeleton className="h-4 w-36 rounded-md" />
            <Skeleton className="h-5 w-24 rounded-md" />
          </div>
          <Skeleton className="mt-4 h-2 w-full rounded-full" />
          <Skeleton className="mt-5 h-13 w-full" />
        </div>
        <Skeleton className="mt-6 h-28" />
      </SkeletonCard>
    </div>
  )
}
