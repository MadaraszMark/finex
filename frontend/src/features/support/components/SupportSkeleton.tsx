import { SkeletonCard, SkeletonChips } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// Egy beszélgetés váza: fejléc (cím, azonosító, állapot), üzenetbuborékok felváltva, alul az üzenetíró
export function ConversationSkeleton() {
  return (
    <div className="flex h-full min-h-[32rem] flex-col">
      <div className="flex items-start gap-3 border-b border-border pb-4">
        <div className="flex-1">
          <Skeleton className="h-6 w-60 max-w-full rounded-lg" />
          <Skeleton className="mt-2 h-3.5 w-16 rounded-md" />
        </div>
        <Skeleton className="h-6 w-24 rounded-full" />
      </div>
      <div className="flex-1 space-y-4 py-6">
        <div className="flex justify-end">
          <Skeleton className="h-16 w-3/5 rounded-3xl" />
        </div>
        <div className="flex">
          <Skeleton className="h-20 w-3/5 rounded-3xl" />
        </div>
        <div className="flex justify-end">
          <Skeleton className="h-12 w-2/5 rounded-3xl" />
        </div>
      </div>
      <div className="flex items-end gap-3 border-t border-border pt-4">
        <Skeleton className="h-13 flex-1" />
        <Skeleton className="size-13 shrink-0 rounded-2xl" />
      </div>
    </div>
  )
}

// Az ügyfélszolgálat (és az admin ticketkezelő) váza: balra a ticketek listája, jobbra a beszélgetés, egyforma
// magasan; az adminnál a lista fölött az állapotszűrő
export default function SupportSkeleton({ filters = false }: { filters?: boolean }) {
  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-[22rem_minmax(0,1fr)]">
      <SkeletonCard className="p-4 sm:p-5">
        {filters ? <SkeletonChips count={4} className="mb-4" /> : <Skeleton className="mb-3 h-4 w-16 rounded-md" />}
        <div className="space-y-2">
          {[0, 1, 2].map((index) => (
            <Skeleton key={index} className="h-24" />
          ))}
        </div>
      </SkeletonCard>
      <SkeletonCard className="hidden lg:flex">
        <ConversationSkeleton />
      </SkeletonCard>
    </div>
  )
}
