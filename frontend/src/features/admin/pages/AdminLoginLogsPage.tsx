import { Laptop, ScrollText, Search, Smartphone, UserRound, X } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import type { LoginLogFilter } from '../../../api/admin'
import { getErrorMessage } from '../../../api/client'
import type { LoginStatus } from '../../../api/types'
import Badge from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import ChipGroup from '../../../components/ui/ChipGroup'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import Pagination from '../../../components/ui/Pagination'
import QueryError from '../../../components/ui/QueryError'
import { Table, Td, Th, Tr } from '../../../components/ui/Table'
import TextField from '../../../components/ui/TextField'
import { cn } from '../../../lib/cn'
import { toIsoDate } from '../../../lib/dates'
import { formatDateTime } from '../../../lib/format'
import { describeUserAgent, isMobileUserAgent } from '../../../lib/userAgent'
import { TableRowsSkeleton } from '../components/AdminSkeletons'
import UserDetailsDialog from '../components/UserDetailsDialog'
import { useAdminLoginLogs } from '../queries'

type StatusFilter = LoginStatus | 'ALL'
type FilterParam = 'status' | 'user' | 'ip' | 'from' | 'to'

const statusOptions: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: 'Mind' },
  { value: 'SUCCESS', label: 'Sikeres' },
  { value: 'FAILED', label: 'Sikertelen' },
]

const filterParams: FilterParam[] = ['status', 'user', 'ip', 'from', 'to']

// A szűrők a címsorban vannak, így egy keresés linkként továbbküldhető
function readFilter(params: URLSearchParams): LoginLogFilter {
  const status = params.get('status')
  return {
    status: status === 'SUCCESS' || status === 'FAILED' ? status : undefined,
    userId: Number(params.get('user')) || undefined,
    ipAddress: params.get('ip') || undefined,
    from: params.get('from') || undefined,
    to: params.get('to') || undefined,
  }
}

// Belépési napló biztonsági vizsgálathoz: ki, mikor, honnan és milyen eszközről
export default function AdminLoginLogsPage() {
  const [params, setParams] = useSearchParams()
  const filter = readFilter(params)
  const page = Math.max(Number(params.get('page')) || 0, 0)
  const logsQuery = useAdminLoginLogs(filter, page)
  const [userId, setUserId] = useState<number>()
  const [today] = useState(() => toIsoDate(new Date()))

  const logs = logsQuery.data?.content ?? []
  const activeCount = filterParams.filter((param) => params.has(param)).length

  // Szűrőváltáskor az első oldalra ugrunk
  function update(patch: Partial<Record<FilterParam, string | undefined>>) {
    const next = new URLSearchParams(params)
    for (const [key, value] of Object.entries(patch)) {
      if (value) {
        next.set(key, value)
      } else {
        next.delete(key)
      }
    }
    next.delete('page')
    setParams(next)
  }

  function applyIp(value: string) {
    if (value.trim() !== (filter.ipAddress ?? '')) {
      update({ ip: value.trim() || undefined })
    }
  }

  function goToPage(next: number) {
    const search = new URLSearchParams(params)
    search.set('page', String(next))
    setParams(search)
  }

  return (
    <>
      <PageHeader eyebrow="Adminisztráció" title="Belépési napló" description="Sikeres és sikertelen belépések biztonsági vizsgálathoz." />

      <Card className="p-5 sm:p-6">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_12rem_12rem]">
          {/* Az IP pontos egyezésre szűr, ezért Enterre vagy kilépéskor keres */}
          <form
            key={filter.ipAddress ?? ''}
            onSubmit={(event) => {
              event.preventDefault()
              applyIp(String(new FormData(event.currentTarget).get('ip') ?? ''))
            }}
            className="sm:col-span-2 xl:col-span-1"
          >
            <TextField
              name="ip"
              label="IP-cím"
              type="search"
              placeholder="pl. 192.168.1.10"
              leftIcon={<Search />}
              defaultValue={filter.ipAddress}
              onBlur={(event) => applyIp(event.target.value)}
            />
          </form>
          <TextField label="Kezdő nap" type="date" value={filter.from ?? ''} max={filter.to ?? today} onChange={(event) => update({ from: event.target.value || undefined })} />
          <TextField label="Záró nap" type="date" value={filter.to ?? ''} min={filter.from} max={today} onChange={(event) => update({ to: event.target.value || undefined })} />
        </div>

        <div className="mt-4 flex flex-wrap items-center gap-2">
          <ChipGroup label="Eredmény" value={filter.status ?? 'ALL'} options={statusOptions} onChange={(value) => update({ status: value === 'ALL' ? undefined : value })} />
          {filter.userId !== undefined && (
            <button
              type="button"
              aria-label={`Szűrő törlése: ügyfél #${filter.userId}`}
              onClick={() => update({ user: undefined })}
              className="inline-flex items-center gap-1.5 rounded-full bg-brand-100 py-1.5 pr-2.5 pl-3.5 text-sm font-semibold text-brand-700 transition hover:bg-brand-200 focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none dark:bg-brand-950 dark:text-brand-300"
            >
              <UserRound aria-hidden className="size-4" />
              Ügyfél #{filter.userId}
              <X aria-hidden className="size-4" />
            </button>
          )}
          {activeCount > 0 && (
            <Button variant="ghost" size="sm" leftIcon={<X />} onClick={() => setParams(new URLSearchParams())} className="sm:ml-auto">
              Szűrők törlése ({activeCount})
            </Button>
          )}
        </div>

        <div className={cn('mt-5 transition-opacity duration-300', logsQuery.isPlaceholderData && 'opacity-50')}>
          {logsQuery.isPending ? (
            <TableRowsSkeleton />
          ) : logsQuery.isError ? (
            <QueryError message={getErrorMessage(logsQuery.error)} retrying={logsQuery.isFetching} onRetry={() => void logsQuery.refetch()} />
          ) : logs.length === 0 ? (
            <EmptyState icon={<ScrollText />} title="Nincs találat" description="Ezekkel a szűrőkkel nincs naplóbejegyzés." />
          ) : (
            <>
              <p className="mb-2 text-sm text-muted-foreground">{logsQuery.data.page.totalElements} bejegyzés</p>
              <Table caption="Belépési napló" minWidth="48rem">
                <thead>
                  <tr>
                    <Th>Időpont</Th>
                    <Th>Felhasználó</Th>
                    <Th>Eredmény</Th>
                    <Th>IP-cím</Th>
                    <Th>Eszköz</Th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map((log) => (
                    <Tr key={log.id} className={cn(log.status === 'FAILED' && 'bg-danger/[0.03]')}>
                      <Td className="whitespace-nowrap text-muted-foreground tabular-nums">{formatDateTime(log.createdAt)}</Td>
                      <Td>
                        {log.userId !== null ? (
                          <button
                            type="button"
                            onClick={() => setUserId(log.userId ?? undefined)}
                            className="rounded-lg text-left font-semibold text-foreground transition hover:text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
                          >
                            {log.email}
                          </button>
                        ) : (
                          <span className="text-muted-foreground">
                            {log.email} <span className="text-xs">(nincs ilyen fiók)</span>
                          </span>
                        )}
                      </Td>
                      <Td>
                        <Badge tone={log.status === 'SUCCESS' ? 'success' : 'danger'}>{log.status === 'SUCCESS' ? 'Sikeres' : 'Sikertelen'}</Badge>
                        {log.failureReason && <span className="mt-1 block text-xs text-muted-foreground">{log.failureReason}</span>}
                      </Td>
                      <Td>
                        {log.ipAddress ? (
                          <button
                            type="button"
                            title="Szűrés erre az IP-címre"
                            onClick={() => update({ ip: log.ipAddress ?? undefined })}
                            className="rounded-lg font-mono text-xs text-foreground transition hover:text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
                          >
                            {log.ipAddress}
                          </button>
                        ) : (
                          <span className="text-muted-foreground">–</span>
                        )}
                      </Td>
                      <Td>
                        <span className="flex items-center gap-2 whitespace-nowrap text-muted-foreground">
                          {isMobileUserAgent(log.userAgent) ? <Smartphone aria-hidden className="size-4" /> : <Laptop aria-hidden className="size-4" />}
                          {describeUserAgent(log.userAgent)}
                        </span>
                      </Td>
                    </Tr>
                  ))}
                </tbody>
              </Table>
            </>
          )}
        </div>

        {logsQuery.data && (
          <div className="mt-5">
            <Pagination page={page} totalPages={logsQuery.data.page.totalPages} onPageChange={goToPage} />
          </div>
        )}
      </Card>

      <UserDetailsDialog userId={userId} onClose={() => setUserId(undefined)} />
    </>
  )
}
