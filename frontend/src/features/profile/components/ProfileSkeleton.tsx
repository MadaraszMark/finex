import { SkeletonCard, SkeletonCardHeader, SkeletonField, SkeletonRows } from '../../../components/skeletons/SkeletonKit'
import Skeleton from '../../../components/ui/Skeleton'

// A Profil oldal váza: a felső névjegy, alatta két egyforma sor (adatok és jelszó, belépések és megjelenés)
export default function ProfileSkeleton() {
  return (
    <>
      <SkeletonCard className="gap-5 p-6 sm:flex-row sm:items-center sm:p-8">
        <Skeleton className="size-20 shrink-0 rounded-[1.75rem]" />
        <div className="flex-1">
          <Skeleton className="h-7 w-52 max-w-full rounded-lg" />
          <Skeleton className="mt-3 h-4 w-80 max-w-full rounded-md" />
        </div>
        <Skeleton className="h-8 w-28 rounded-full" />
      </SkeletonCard>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <SkeletonCard className="p-6 sm:p-7">
          <SkeletonCardHeader />
          <div className="mt-6 grid grid-cols-1 gap-5 sm:grid-cols-2">
            <SkeletonField />
            <SkeletonField />
          </div>
          <SkeletonField className="mt-5" />
          <SkeletonField className="mt-5" />
          <div className="mt-auto flex justify-end pt-5">
            <Skeleton className="h-11 w-28 rounded-2xl" />
          </div>
        </SkeletonCard>
        <SkeletonCard className="p-6 sm:p-7">
          <SkeletonCardHeader />
          <SkeletonField className="mt-6" />
          <SkeletonField className="mt-5" />
          <SkeletonField className="mt-5" />
          <div className="mt-auto flex justify-end pt-5">
            <Skeleton className="h-11 w-48 rounded-2xl" />
          </div>
        </SkeletonCard>
      </div>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <SkeletonCard className="p-6 sm:p-7">
          <SkeletonCardHeader />
          <SkeletonRows count={5} className="mt-4" />
        </SkeletonCard>
        <SkeletonCard className="p-6 sm:p-7">
          <SkeletonCardHeader />
          <div className="mt-5 grid flex-1 auto-rows-fr gap-3">
            {[0, 1, 2].map((index) => (
              <Skeleton key={index} className="min-h-20" />
            ))}
          </div>
        </SkeletonCard>
      </div>
    </>
  )
}
