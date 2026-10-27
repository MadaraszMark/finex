import { History, X } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import type { AuditLogFilter } from '../../../api/admin'
import { getErrorMessage } from '../../../api/client'
import type { AuditAction, AuditTargetType } from '../../../api/types'
import Badge, { type BadgeTone } from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import Pagination from '../../../components/ui/Pagination'
import QueryError from '../../../components/ui/QueryError'
import Select from '../../../components/ui/Select'
import { Table, Td, Th, Tr } from '../../../components/ui/Table'
import TextField from '../../../components/ui/TextField'
import { cn } from '../../../lib/cn'
import { toIsoDate } from '../../../lib/dates'
import { formatDateTime } from '../../../lib/format'
import { TableRowsSkeleton } from '../components/AdminSkeletons'
import { useAuditLog } from '../queries'

const auditActionLabels: Record<AuditAction, string> = {
  USER_STATUS_CHANGED: 'Felhasználó letiltása / feloldása',
  USER_ROLE_CHANGED: 'Szerepkör módosítása',
  ACCOUNT_STATUS_CHANGED: 'Számla státuszának módosítása',
  ACCOUNT_DEPOSIT: 'Készpénzbefizetés rögzítése',
  TRANSACTION_REVERSED: 'Sztornó',
  CATEGORY_CREATED: 'Kategória létrehozása',
  CATEGORY_UPDATED: 'Kategória módosítása',
  CATEGORY_DELETED: 'Kategória törlése',
  TICKET_STATUS_CHANGED: 'Ticket státuszának módosítása',
  JOB_RUN: 'Ütemezett feladat kézi indítása',
}

// A pénzt mozgató és a hozzáférést érintő műveletek szembeötlőbbek
const actionTones: Partial<Record<AuditAction, BadgeTone>> = {
  ACCOUNT_DEPOSIT: 'warning',
  TRANSACTION_REVERSED: 'warning',
  USER_STATUS_CHANGED: 'danger',
  USER_ROLE_CHANGED: 'danger',
  ACCOUNT_STATUS_CHANGED: 'danger',
}

const targetLabels: Record<AuditTargetType, string> = {
  USER: 'Felhasználó',
  ACCOUNT: 'Számla',
  TRANSACTION: 'Tétel',
  CATEGORY: 'Kategória',
  SUPPORT_TICKET: 'Ticket',
  JOB: 'Feladat',
}

type FilterParam = 'action' | 'from' | 'to'
const filterParams: FilterParam[] = ['action', 'from', 'to']

function isAuditAction(value: string | null): value is AuditAction {
  return value !== null && value in auditActionLabels
}

// A szűrők a címsorban vannak, így egy keresés linkként továbbküldhető
function readFilter(params: URLSearchParams): AuditLogFilter {
  const action = params.get('action')
  return {
    action: isAuditAction(action) ? action : undefined,
    from: params.get('from') || undefined,
    to: params.get('to') || undefined,
  }
}

// Admin műveletek naplója; csak bővíthető (az adatbázis tiltja a módosítást és a törlést)
export default function AdminAuditLogPage() {
  const [params, setParams] = useSearchParams()
  const filter = readFilter(params)
  const page = Math.max(Number(params.get('page')) || 0, 0)
  const logQuery = useAuditLog(filter, page)
  const [today] = useState(() => toIsoDate(new Date()))

  const entries = logQuery.data?.content ?? []
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

  function goToPage(next: number) {
    const search = new URLSearchParams(params)
    search.set('page', String(next))
    setParams(search)
  }

  return (
    <>
      <PageHeader eyebrow="Adminisztráció" title="Audit napló" description="Ki, mikor és mit módosított az adminfelületen." />

      <Card className="p-5 sm:p-6">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_12rem_12rem]">
          <Select label="Művelet" value={filter.action ?? ''} onChange={(event) => update({ action: event.target.value || undefined })} className="sm:col-span-2 xl:col-span-1">
            <option value="">Minden művelet</option>
            {(Object.keys(auditActionLabels) as AuditAction[]).map((action) => (
              <option key={action} value={action}>
                {auditActionLabels[action]}
              </option>
            ))}
          </Select>
          <TextField label="Kezdő nap" type="date" value={filter.from ?? ''} max={filter.to ?? today} onChange={(event) => update({ from: event.target.value || undefined })} />
          <TextField label="Záró nap" type="date" value={filter.to ?? ''} min={filter.from} max={today} onChange={(event) => update({ to: event.target.value || undefined })} />
        </div>

        {activeCount > 0 && (
          <div className="mt-4 flex justify-end">
            <Button variant="ghost" size="sm" leftIcon={<X />} onClick={() => setParams(new URLSearchParams())}>
              Szűrők törlése ({activeCount})
            </Button>
          </div>
        )}

        <div className={cn('mt-5 transition-opacity duration-300', logQuery.isPlaceholderData && 'opacity-50')}>
          {logQuery.isPending ? (
            <TableRowsSkeleton />
          ) : logQuery.isError ? (
            <QueryError message={getErrorMessage(logQuery.error)} retrying={logQuery.isFetching} onRetry={() => void logQuery.refetch()} />
          ) : entries.length === 0 ? (
            <EmptyState icon={<History />} title="Nincs bejegyzés" description="Ezekkel a szűrőkkel nincs adminművelet a naplóban." />
          ) : (
            <>
              <p className="mb-2 text-sm text-muted-foreground">{logQuery.data.page.totalElements} bejegyzés</p>
              <Table caption="Audit napló" minWidth="52rem">
                <thead>
                  <tr>
                    <Th>Időpont</Th>
                    <Th>Admin</Th>
                    <Th>Művelet</Th>
                    <Th>Érintett</Th>
                    <Th>Részletek</Th>
                  </tr>
                </thead>
                <tbody>
                  {entries.map((entry) => (
                    <Tr key={entry.id}>
                      <Td className="whitespace-nowrap text-muted-foreground tabular-nums">{formatDateTime(entry.createdAt)}</Td>
                      <Td>
                        <span className="block font-semibold text-foreground">{entry.actorEmail}</span>
                        {entry.ipAddress && <span className="block font-mono text-xs text-muted-foreground">{entry.ipAddress}</span>}
                      </Td>
                      <Td>
                        <Badge tone={actionTones[entry.action] ?? 'neutral'}>{auditActionLabels[entry.action]}</Badge>
                      </Td>
                      <Td className="whitespace-nowrap text-muted-foreground">
                        {targetLabels[entry.targetType]}
                        {entry.targetId !== null && ` #${entry.targetId}`}
                      </Td>
                      <Td className="text-sm text-foreground">{entry.details ?? '–'}</Td>
                    </Tr>
                  ))}
                </tbody>
              </Table>
            </>
          )}
        </div>

        {logQuery.data && (
          <div className="mt-5">
            <Pagination page={page} totalPages={logQuery.data.page.totalPages} onPageChange={goToPage} />
          </div>
        )}
      </Card>
    </>
  )
}
