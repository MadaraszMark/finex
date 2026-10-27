import { ArrowLeft, CircleCheck, Inbox, RotateCcw } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { TicketStatus } from '../../../api/types'
import Button from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import ChipGroup from '../../../components/ui/ChipGroup'
import EmptyState from '../../../components/ui/EmptyState'
import IconButton from '../../../components/ui/IconButton'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import Skeleton from '../../../components/ui/Skeleton'
import { useMediaQuery } from '../../../hooks/useMediaQuery'
import { cn } from '../../../lib/cn'
import { ConversationSkeleton } from '../../support/components/SupportSkeleton'
import TicketConversation from '../../support/components/TicketConversation'
import TicketList from '../../support/components/TicketList'
import { useAdminTicket, useAdminTickets, useReplyToTicket, useUpdateTicketStatus } from '../queries'

type StatusFilter = TicketStatus | 'ALL'

const statusOptions: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: 'Összes' },
  { value: 'OPEN', label: 'Válaszra vár' },
  { value: 'IN_PROGRESS', label: 'Folyamatban' },
  { value: 'RESOLVED', label: 'Lezárva' },
]

function parseStatus(value: string | null): TicketStatus | undefined {
  return statusOptions.find((option) => option.value === value && option.value !== 'ALL')?.value as TicketStatus | undefined
}

// Ticketkezelő: a szűrő és a kiválasztott ticket a címsorban van (?status=…&ticket=…)
export default function AdminTicketsPage() {
  const [params, setParams] = useSearchParams()
  const status = parseStatus(params.get('status'))
  const ticketsQuery = useAdminTickets(status)
  const desktop = useMediaQuery('(min-width: 1024px)')
  const [now] = useState(() => new Date())

  const tickets = ticketsQuery.data ?? []
  const requestedId = Number(params.get('ticket')) || undefined
  // Asztali nézetben kiválasztás nélkül is a lista első ticketje nyílik meg
  const selectedId = requestedId ?? (desktop ? tickets[0]?.id : undefined)
  const ticketQuery = useAdminTicket(selectedId)
  const reply = useReplyToTicket()
  const updateStatus = useUpdateTicketStatus()

  function show(nextStatus: TicketStatus | undefined, ticketId: number | undefined) {
    const search = new URLSearchParams()
    if (nextStatus) {
      search.set('status', nextStatus)
    }
    if (ticketId !== undefined) {
      search.set('ticket', String(ticketId))
    }
    setParams(search)
    reply.reset()
  }

  function changeStatus(id: number, next: TicketStatus) {
    updateStatus.mutate(
      { id, status: next },
      {
        onSuccess: () => toast.success(next === 'RESOLVED' ? 'Lezártad a ticketet' : 'Újranyitottad a ticketet', { description: 'Az ügyfél értesítést kapott.' }),
        onError: (error) => toast.error(getErrorMessage(error)),
      },
    )
  }

  return (
    <>
      <PageHeader eyebrow="Adminisztráció" title="Ticketek" description="Válaszolj az ügyfelek kérdéseire, és zárd le a megoldott ügyeket." />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[22rem_minmax(0,1fr)]">
        <Card className={cn('p-4 sm:p-5', selectedId !== undefined && 'hidden lg:block')}>
          <ChipGroup
            label="Állapot"
            value={status ?? 'ALL'}
            options={statusOptions}
            onChange={(value) => show(value === 'ALL' ? undefined : value, undefined)}
            className="mb-4"
          />

          <div className={cn('transition-opacity duration-300', ticketsQuery.isPlaceholderData && 'opacity-50')}>
            {ticketsQuery.isPending ? (
              <div className="space-y-2">
                {[0, 1, 2].map((index) => (
                  <Skeleton key={index} className="h-24 rounded-2xl" />
                ))}
              </div>
            ) : ticketsQuery.isError ? (
              <QueryError message={getErrorMessage(ticketsQuery.error)} retrying={ticketsQuery.isFetching} onRetry={() => void ticketsQuery.refetch()} />
            ) : tickets.length === 0 ? (
              <EmptyState icon={<Inbox />} title="Nincs ilyen ticket" description={status === 'OPEN' ? 'Minden kérdésre válaszoltál.' : undefined} />
            ) : (
              <TicketList tickets={tickets} selectedId={selectedId} onSelect={(id) => show(status, id)} now={now} showCustomer />
            )}
          </div>
        </Card>

        {selectedId !== undefined && (
          <Card className="p-5 sm:p-6">
            {ticketQuery.isPending ? (
              <ConversationSkeleton />
            ) : ticketQuery.isError ? (
              <QueryError message={getErrorMessage(ticketQuery.error)} retrying={ticketQuery.isFetching} onRetry={() => void ticketQuery.refetch()} />
            ) : (
              <TicketConversation
                key={ticketQuery.data.id}
                ticket={ticketQuery.data}
                viewer="staff"
                sending={reply.isPending}
                sendError={reply.isError ? getErrorMessage(reply.error) : undefined}
                onSend={(message) => reply.mutateAsync({ id: ticketQuery.data.id, message })}
                backButton={
                  <IconButton label="Vissza a ticketekhez" onClick={() => show(status, undefined)} className="size-10 lg:hidden">
                    <ArrowLeft />
                  </IconButton>
                }
                headerActions={
                  ticketQuery.data.status === 'RESOLVED' ? (
                    <Button variant="outline" size="sm" leftIcon={<RotateCcw />} loading={updateStatus.isPending} onClick={() => changeStatus(ticketQuery.data.id, 'IN_PROGRESS')}>
                      Újranyitás
                    </Button>
                  ) : (
                    <Button variant="outline" size="sm" leftIcon={<CircleCheck />} loading={updateStatus.isPending} onClick={() => changeStatus(ticketQuery.data.id, 'RESOLVED')}>
                      Lezárás
                    </Button>
                  )
                }
              />
            )}
          </Card>
        )}
      </div>
    </>
  )
}
