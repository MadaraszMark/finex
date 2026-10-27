import { PageSkeleton, SkeletonCard, SkeletonCardHeader, SkeletonChips, SkeletonStatTile } from '../../../components/skeletons/SkeletonKit'
import { cardSurface } from '../../../components/ui/cardStyles'
import Skeleton from '../../../components/ui/Skeleton'
import { cn } from '../../../lib/cn'
import SupportSkeleton from '../../support/components/SupportSkeleton'

// Az admin áttekintő váza: mutatók, két kártya és a főkönyvi egyeztetés
export function AdminDashboardSkeleton() {
  return (
    <>
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4 lg:gap-6">
        {Array.from({ length: 8 }, (_, index) => (
          <SkeletonStatTile key={index} />
        ))}
      </div>
      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        {[0, 1].map((index) => (
          <SkeletonCard key={index} className="p-6 sm:p-6">
            <SkeletonCardHeader action={index === 1} />
            <div className="mt-5 space-y-3">
              <Skeleton className="h-[4.5rem]" />
              <Skeleton className="h-[4.5rem]" />
            </div>
          </SkeletonCard>
        ))}
        <SkeletonCard className="p-6 sm:p-6 lg:col-span-2">
          <SkeletonCardHeader action />
          <Skeleton className="mt-5 h-[4.5rem]" />
        </SkeletonCard>
      </div>
    </>
  )
}

export function TableRowsSkeleton({ rows = 6 }: { rows?: number }) {
  return (
    <div className="space-y-3">
      <Skeleton className="h-4 w-full max-w-md rounded-md" />
      {Array.from({ length: rows }, (_, index) => (
        <Skeleton key={index} className="h-12" />
      ))}
    </div>
  )
}

// Táblázatos admin oldalak váza: a szűrők és a táblázat egy kártyán
export function AdminTableSkeleton({ filters }: { filters: 'search' | 'chips' | 'logs' | 'audit' }) {
  return (
    <SkeletonCard>
      {filters === 'search' && (
        <div className="max-w-md">
          <Skeleton className="h-4 w-20 rounded-md" />
          <Skeleton className="mt-2 h-13 w-full" />
        </div>
      )}
      {filters === 'chips' && <SkeletonChips count={5} />}
      {(filters === 'logs' || filters === 'audit') && (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_12rem_12rem]">
            {[0, 1, 2].map((index) => (
              <div key={index} className={cn(index === 0 && 'sm:col-span-2 xl:col-span-1')}>
                <Skeleton className="h-4 w-20 rounded-md" />
                <Skeleton className="mt-2 h-13 w-full" />
              </div>
            ))}
          </div>
          {filters === 'logs' && <SkeletonChips count={3} className="mt-4" />}
        </>
      )}
      <div className="mt-5">
        <TableRowsSkeleton />
      </div>
    </SkeletonCard>
  )
}

export function AdminCategoriesSkeleton() {
  return (
    <div className="grid grid-cols-[repeat(auto-fill,minmax(min(18rem,100%),1fr))] gap-3">
      {Array.from({ length: 9 }, (_, index) => (
        <div key={index} className={cn(cardSurface, 'flex items-center gap-3 p-4')}>
          <Skeleton className="size-11 shrink-0 rounded-2xl" />
          <Skeleton className="h-4 flex-1 rounded-md" />
          <Skeleton className="size-9 shrink-0 rounded-2xl" />
          <Skeleton className="size-9 shrink-0 rounded-2xl" />
        </div>
      ))}
    </div>
  )
}

// Útvonal szerinti váz, amíg az oldal kódja és a szerepkör-ellenőrzés betöltődik
export function AdminPageSkeleton({ pathname }: { pathname: string }) {
  if (pathname.startsWith('/admin/users')) {
    return <AdminTableSkeleton filters="search" />
  }
  if (pathname.startsWith('/admin/accounts')) {
    return <AdminTableSkeleton filters="chips" />
  }
  if (pathname.startsWith('/admin/audit-log')) {
    return <AdminTableSkeleton filters="audit" />
  }
  if (pathname.startsWith('/admin/login-logs')) {
    return <AdminTableSkeleton filters="logs" />
  }
  if (pathname.startsWith('/admin/support-tickets')) {
    return <SupportSkeleton filters />
  }
  if (pathname.startsWith('/admin/categories')) {
    return <AdminCategoriesSkeleton />
  }
  return <AdminDashboardSkeleton />
}

export function AdminRouteSkeleton({ pathname }: { pathname: string }) {
  return (
    <PageSkeleton header={{ eyebrow: true, action: pathname.startsWith('/admin/categories') }}>
      <AdminPageSkeleton pathname={pathname} />
    </PageSkeleton>
  )
}
