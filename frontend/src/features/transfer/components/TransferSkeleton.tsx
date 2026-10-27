import { SkeletonCard } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// Az utalás váza: balra a varázsló, jobbra az összefoglaló
export default function TransferSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_20rem]">
      <SkeletonCard className="min-h-[28rem] p-5 sm:p-8">
        <div className="flex items-center gap-2">
          {[0, 1, 2, 3].map((index) => (
            <div key={index} className="flex flex-1 items-center gap-2">
              <Skeleton className="size-7 shrink-0 rounded-full" />
              <Skeleton className="hidden h-4 w-16 rounded-md sm:block" />
            </div>
          ))}
        </div>
        <Skeleton className="mt-8 h-6 w-64 max-w-full rounded-lg" />
        <div className="mt-4 space-y-2">
          <Skeleton className="h-[4.5rem]" />
          <Skeleton className="h-[4.5rem]" />
        </div>
        <div className="mt-auto pt-8">
          <div className="flex justify-between gap-3 border-t border-border pt-6">
            <Skeleton className="h-11 w-28 rounded-2xl" />
            <Skeleton className="h-11 w-32 rounded-2xl" />
          </div>
        </div>
      </SkeletonCard>

      <SkeletonCard className="hidden p-6 lg:flex">
        <Skeleton className="h-5 w-32 rounded-lg" />
        <Skeleton className="mt-5 h-4 w-16 rounded-md" />
        <Skeleton className="mt-2 h-7 w-28 rounded-lg" />
        <Skeleton className="mt-5 h-72" />
      </SkeletonCard>
    </div>
  )
}
