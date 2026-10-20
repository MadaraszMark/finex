import type { TicketStatus } from '../../api/types'
import type { BadgeTone } from '../../components/ui/Badge'

// A ticket állapotai emberi nyelven és a jelvényük színe
export const ticketStatusLabels: Record<TicketStatus, string> = {
  OPEN: 'Válaszra vár',
  IN_PROGRESS: 'Folyamatban',
  RESOLVED: 'Lezárva',
}

export const ticketStatusTones: Record<TicketStatus, BadgeTone> = {
  OPEN: 'warning',
  IN_PROGRESS: 'brand',
  RESOLVED: 'success',
}
