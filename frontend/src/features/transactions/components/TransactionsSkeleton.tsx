import { SkeletonCard, SkeletonChips, SkeletonRows } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A tranzakciólista sorainak váza (napfejléccel)
export function TransactionListSkeleton({ rows = 6 }: { rows?: number }) {
  return (
    <div className="mt-4">
      <Skeleton className="mb-1 h-3.5 w-40 rounded-md" />
      <SkeletonRows count={rows} />
    </div>
  )
}

// A Tranzakciók oldal váza: a szűrők kártyája és a lista
export default function TransactionsSkeleton() {
  return (
    <>
      <SkeletonCard>
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <div className="sm:flex-1">
            <Skeleton className="h-4 w-20 rounded-md" />
            <Skeleton className="mt-2 h-13 w-full" />
          </div>
          <Skeleton className="h-13 w-full rounded-2xl sm:w-48" />
        </div>
        <SkeletonChips count={5} className="mt-4" />
        <SkeletonChips count={5} className="mt-3" />
      </SkeletonCard>
      <SkeletonCard className="mt-6">
        <Skeleton className="h-6 w-24 rounded-lg" />
        <TransactionListSkeleton rows={8} />
      </SkeletonCard>
    </>
  )
}
