import { SkeletonCard, SkeletonChips } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// Az értesítések sorainak váza (napfejléccel)
export function NotificationRowsSkeleton({ count }: { count: number }) {
  return (
    <div>
      <Skeleton className="mb-3 h-3.5 w-24 rounded-md" />
      <div className="space-y-2">
        {Array.from({ length: count }, (_, index) => (
          <div key={index} className="flex items-start gap-3.5 p-3">
            <Skeleton className="size-10 shrink-0 rounded-xl" />
            <div className="flex-1">
              <Skeleton className="h-4 w-40 rounded-md" />
              <Skeleton className="mt-2 h-3.5 w-4/5 rounded-md" />
              <Skeleton className="mt-2 h-3 w-28 rounded-md" />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

// Az Értesítések oldal váza: szűrők és a napok szerinti lista
export default function NotificationsSkeleton() {
  return (
    <SkeletonCard className="p-4 sm:p-6">
      <SkeletonChips count={7} />
      <div className="mt-5">
        <NotificationRowsSkeleton count={5} />
      </div>
    </SkeletonCard>
  )
}
