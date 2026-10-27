import { ArrowRight, Inbox, MessagesSquare } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router'
import { getErrorMessage } from '../../../api/client'
import { Card, CardHeader } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import QueryError from '../../../components/ui/QueryError'
import Skeleton from '../../../components/ui/Skeleton'
import { formatRelativeTime } from '../../../lib/format'
import { useAdminTickets } from '../queries'

// A legfrissebb (legfeljebb három) válaszra váró ticket az áttekintőn
export default function OpenTicketsCard() {
  const ticketsQuery = useAdminTickets('OPEN')
  const [now] = useState(() => new Date())
  const tickets = (ticketsQuery.data ?? []).slice(0, 3)

  return (
    <Card className="flex flex-col p-6">
      <CardHeader
        title="Válaszra váró ticketek"
        description="A legfrissebb kérdések, amelyekre még nem válaszoltatok."
        action={
          <Link
            to="/admin/support-tickets?status=OPEN"
            className="group inline-flex items-center gap-1 rounded-lg text-sm font-semibold text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
          >
            Összes
            <ArrowRight aria-hidden className="size-4 transition-transform group-hover:translate-x-0.5" />
          </Link>
        }
      />

      {ticketsQuery.isPending ? (
        <div className="mt-5 space-y-3">
          {[0, 1, 2].map((index) => (
            <Skeleton key={index} className="h-16 rounded-2xl" />
          ))}
        </div>
      ) : ticketsQuery.isError ? (
        <QueryError message={getErrorMessage(ticketsQuery.error)} retrying={ticketsQuery.isFetching} onRetry={() => void ticketsQuery.refetch()} />
      ) : tickets.length === 0 ? (
        <EmptyState icon={<Inbox />} title="Nincs válaszra váró ticket" description="Minden kérdésre válaszoltatok." className="my-auto py-6" />
      ) : (
        <ul className="mt-5 space-y-3">
          {tickets.map((ticket) => (
            <li key={ticket.id}>
              <Link
                to={`/admin/support-tickets?status=OPEN&ticket=${ticket.id}`}
                className="flex items-center gap-3 rounded-2xl bg-muted/70 p-4 transition hover:bg-muted focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
              >
                <MessagesSquare aria-hidden className="size-5 shrink-0 text-primary" />
                <span className="min-w-0 flex-1">
                  <span className="block truncate font-semibold text-foreground">{ticket.title}</span>
                  <span className="block truncate text-sm text-muted-foreground">
                    {ticket.userFullName} · {formatRelativeTime(ticket.updatedAt, now)}
                  </span>
                </span>
                <ArrowRight aria-hidden className="size-4 shrink-0 text-muted-foreground" />
              </Link>
            </li>
          ))}
        </ul>
      )}
    </Card>
  )
}
