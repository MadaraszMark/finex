import { SkeletonCard } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A kedvezményezettek váza: a kártyák rácsa
export default function BeneficiariesSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3">
      {[0, 1, 2].map((index) => (
        <SkeletonCard key={index} className="p-5 sm:p-5">
          <div className="flex items-start gap-4">
            <Skeleton className="size-12 shrink-0 rounded-2xl" />
            <div className="flex-1">
              <Skeleton className="h-5 w-32 rounded-lg" />
              <Skeleton className="mt-2 h-3.5 w-20 rounded-md" />
            </div>
          </div>
          <Skeleton className="mt-4 h-12" />
          <div className="mt-auto flex items-center gap-2 pt-5">
            <Skeleton className="h-9 flex-1 rounded-2xl" />
            <Skeleton className="size-9 rounded-2xl" />
            <Skeleton className="size-9 rounded-2xl" />
          </div>
        </SkeletonCard>
      ))}
    </div>
  )
}
