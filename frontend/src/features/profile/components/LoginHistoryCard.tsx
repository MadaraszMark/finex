import { ChevronDown, CircleCheck, CircleX, LogOut, Monitor, ShieldAlert, Smartphone } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import { SkeletonRows } from '../../../components/skeletons/SkeletonKit'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import ConfirmDialog from '../../../components/ui/ConfirmDialog'
import QueryError from '../../../components/ui/QueryError'
import { cn } from '../../../lib/cn'
import { formatDateTime } from '../../../lib/format'
import { describeIpAddress, describeUserAgent, isMobileUserAgent } from '../../../lib/userAgent'
import { useLogoutEverywhere, useMyLoginLogs } from '../queries'

// Alapból ennyi belépés látszik, így a kártya a mellette álló kártyával egy magas marad
const VISIBLE_COUNT = 5

// Saját belépési napló; a "Korábbi belépések" előbb a betöltötteket mutatja, aztán lapoz
export default function LoginHistoryCard() {
  const logsQuery = useMyLoginLogs()
  const [now] = useState(() => new Date())
  const [showAll, setShowAll] = useState(false)
  const logs = logsQuery.data?.pages.flatMap((page) => page.content) ?? []
  const recentFailures = logs.filter((log) => log.status === 'FAILED' && now.getTime() - new Date(log.createdAt).getTime() < 7 * 86_400_000).length
  const visibleLogs = showAll ? logs : logs.slice(0, VISIBLE_COUNT)
  const canShowMore = showAll ? logsQuery.hasNextPage : logs.length > VISIBLE_COUNT
  const logoutEverywhere = useLogoutEverywhere()
  const [confirming, setConfirming] = useState(false)

  function logoutOthers() {
    logoutEverywhere.mutate(undefined, {
      onSuccess: () => toast.success('Minden más eszközön kijelentkeztettünk', { description: 'Ezen az eszközön bejelentkezve maradtál.' }),
      onError: (error) => toast.error(getErrorMessage(error)),
      onSettled: () => setConfirming(false),
    })
  }

  function showMore() {
    if (showAll) {
      void logsQuery.fetchNextPage()
    } else {
      setShowAll(true)
    }
  }

  return (
    <Card className="flex flex-col p-6 sm:p-7">
      <CardHeader title="Belépések" description="Az utolsó belépési próbálkozások a fiókodba." />

      {recentFailures > 0 && (
        <p className="mt-4 flex items-start gap-2.5 rounded-2xl bg-danger/8 px-4 py-3 text-sm text-danger">
          <ShieldAlert aria-hidden className="mt-0.5 size-4 shrink-0" />
          Az elmúlt héten {recentFailures} sikertelen belépési kísérlet volt. Ha nem te voltál, változtass jelszót.
        </p>
      )}

      <div className="mt-4">
        {logsQuery.isPending ? (
          <SkeletonRows count={VISIBLE_COUNT} />
        ) : logsQuery.isError ? (
          <QueryError message={getErrorMessage(logsQuery.error)} retrying={logsQuery.isFetching} onRetry={() => void logsQuery.refetch()} />
        ) : (
          <ul className="divide-y divide-border">
            {visibleLogs.map((log, index) => {
              const success = log.status === 'SUCCESS'
              return (
                <li key={`${log.createdAt}-${index}`} className="flex items-center gap-3.5 py-3">
                  <span className="relative flex size-11 shrink-0 items-center justify-center rounded-2xl bg-muted text-muted-foreground">
                    {isMobileUserAgent(log.userAgent) ? <Smartphone aria-hidden className="size-5" /> : <Monitor aria-hidden className="size-5" />}
                    <span className={cn('absolute -right-1 -bottom-1 rounded-full bg-card', success ? 'text-emerald-600' : 'text-danger')}>
                      {success ? <CircleCheck aria-label="Sikeres" className="size-4" /> : <CircleX aria-label="Sikertelen" className="size-4" />}
                    </span>
                  </span>
                  <span className="min-w-0 flex-1">
                    <span className="flex items-baseline justify-between gap-3">
                      <span className="truncate font-semibold text-foreground">{describeUserAgent(log.userAgent)}</span>
                      <span className="shrink-0 text-xs text-muted-foreground tabular-nums">{formatDateTime(log.createdAt)}</span>
                    </span>
                    <span className="block text-sm text-muted-foreground">
                      {success ? 'Sikeres belépés' : (log.failureReason ?? 'Sikertelen belépés')}
                      {log.ipAddress && ` · ${describeIpAddress(log.ipAddress)}`}
                    </span>
                  </span>
                </li>
              )
            })}
          </ul>
        )}
      </div>

      <div className="mt-auto flex flex-wrap items-center justify-center gap-2 pt-4">
        {canShowMore && (
          <Button variant="outline" size="sm" rightIcon={<ChevronDown />} loading={logsQuery.isFetchingNextPage} onClick={showMore}>
            Korábbi belépések
          </Button>
        )}
        <Button variant="ghost" size="sm" leftIcon={<LogOut />} onClick={() => setConfirming(true)}>
          Kijelentkezés minden más eszközről
        </Button>
      </div>

      <ConfirmDialog
        open={confirming}
        onClose={() => setConfirming(false)}
        onConfirm={logoutOthers}
        loading={logoutEverywhere.isPending}
        title="Kijelentkezel minden más eszközről?"
        description="Minden más böngészőben és telefonon azonnal lejár a bejelentkezésed. Ezen az eszközön bejelentkezve maradsz."
        confirmLabel="Kijelentkeztetés"
        tone="primary"
      />
    </Card>
  )
}
