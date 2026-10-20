import type { TicketStatus } from '../../../api/types'
import Badge from '../../../components/ui/Badge'
import { ticketStatusLabels, ticketStatusTones } from '../ticketStatus'

export default function TicketStatusBadge({ status }: { status: TicketStatus }) {
  return <Badge tone={ticketStatusTones[status]}>{ticketStatusLabels[status]}</Badge>
}
