import { MessagesSquare } from 'lucide-react'
import type { SupportTicketListItem } from '../../../api/types'
import { cn } from '../../../lib/cn'
import { formatRelativeTime } from '../../../lib/format'
import TicketStatusBadge from './TicketStatusBadge'

interface TicketListProps {
  tickets: SupportTicketListItem[]
  selectedId: number | undefined
  onSelect: (id: number) => void
  now: Date
  // Az ügyfélszolgálat látja az ügyfél nevét is
  showCustomer?: boolean
}

// Ticketek listája, a legújabb elöl
export default function TicketList({ tickets, selectedId, onSelect, now, showCustomer = false }: TicketListProps) {
  return (
    <ul className="space-y-2">
      {tickets.map((ticket) => {
        const selected = ticket.id === selectedId
        return (
          <li key={ticket.id}>
            <button
              type="button"
              aria-current={selected ? 'true' : undefined}
              onClick={() => onSelect(ticket.id)}
              className={cn(
                'flex w-full items-start gap-3 rounded-2xl border p-3.5 text-left transition duration-200',
                'focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none',
                selected ? 'border-brand-400 bg-brand-50 dark:border-brand-500/60 dark:bg-brand-950/60' : 'border-border bg-card/70 hover:border-brand-300',
              )}
            >
              <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300">
                <MessagesSquare aria-hidden className="size-5" />
              </span>
              <span className="min-w-0 flex-1">
                <span className="block truncate font-bold text-foreground">{ticket.title}</span>
                <span className="mt-0.5 block truncate text-xs text-muted-foreground">
                  {showCustomer && `${ticket.userFullName} · `}#{ticket.id} · {formatRelativeTime(ticket.updatedAt, now)}
                </span>
                <span className="mt-2 block">
                  <TicketStatusBadge status={ticket.status} />
                </span>
              </span>
            </button>
          </li>
        )
      })}
    </ul>
  )
}
