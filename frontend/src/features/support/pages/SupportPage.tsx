import { ArrowLeft, Headset, MessageCirclePlus } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { getErrorMessage } from '../../../api/client'
import Button from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import IconButton from '../../../components/ui/IconButton'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { useMediaQuery } from '../../../hooks/useMediaQuery'
import { cn } from '../../../lib/cn'
import NewTicketDialog from '../components/NewTicketDialog'
import SupportSkeleton, { ConversationSkeleton } from '../components/SupportSkeleton'
import TicketConversation from '../components/TicketConversation'
import TicketList from '../components/TicketList'
import { useMyTickets, useSendTicketMessage, useTicket } from '../queries'

// Ügyfélszolgálat: bal oldalon a ticketek, jobbra a kiválasztott beszélgetése (mobilon egymás után: lista → beszélgetés).
// A kiválasztott ticket a címsorban van (?ticket=12), így az értesítésből vagy linkből közvetlenül megnyitható.
export default function SupportPage() {
  const ticketsQuery = useMyTickets()
  const [params, setParams] = useSearchParams()
  const desktop = useMediaQuery('(min-width: 1024px)')
  const [creating, setCreating] = useState(false)
  const [now] = useState(() => new Date())

  const tickets = ticketsQuery.data ?? []
  const requestedId = Number(params.get('ticket')) || undefined
  // Asztali nézetben kiválasztás nélkül is a legfrissebb ticket nyílik meg
  const selectedId = requestedId ?? (desktop ? tickets[0]?.id : undefined)
  const ticketQuery = useTicket(selectedId)
  const sendMessage = useSendTicketMessage()

  function select(id: number | undefined) {
    setParams(id === undefined ? new URLSearchParams() : new URLSearchParams({ ticket: String(id) }))
    sendMessage.reset()
  }

  return (
    <>
      <PageHeader
        title="Ügyfélszolgálat"
        description="Kérdésed van? Írj nekünk – általában egy munkanapon belül válaszolunk."
        actions={
          <Button variant="glass" leftIcon={<MessageCirclePlus />} onClick={() => setCreating(true)}>
            Új kérdés
          </Button>
        }
      />

      {ticketsQuery.isPending ? (
        <SupportSkeleton />
      ) : ticketsQuery.isError ? (
        <Card>
          <QueryError message={getErrorMessage(ticketsQuery.error)} retrying={ticketsQuery.isFetching} onRetry={() => void ticketsQuery.refetch()} />
        </Card>
      ) : tickets.length === 0 ? (
        <Card>
          <EmptyState
            icon={<Headset />}
            title="Még nem írtál nekünk"
            description="Ha elakadtál, kérdésed van egy utalásról vagy a kártyádról, írj nyugodtan."
            action={
              <Button leftIcon={<MessageCirclePlus />} onClick={() => setCreating(true)}>
                Kérdés küldése
              </Button>
            }
          />
        </Card>
      ) : (
        // A lista és a beszélgetés egyforma magas (a lista kártyája a beszélgetéséhez nyúlik)
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[22rem_minmax(0,1fr)]">
          <Card className={cn('p-4 sm:p-5', selectedId !== undefined && 'hidden lg:block')}>
            <p className="mb-3 px-1 text-sm font-semibold text-muted-foreground">{tickets.length} ticket</p>
            <TicketList tickets={tickets} selectedId={selectedId} onSelect={select} now={now} />
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
                  viewer="customer"
                  sending={sendMessage.isPending}
                  sendError={sendMessage.isError ? getErrorMessage(sendMessage.error) : undefined}
                  onSend={(message) => sendMessage.mutateAsync({ id: ticketQuery.data.id, message })}
                  backButton={
                    <IconButton label="Vissza a ticketekhez" onClick={() => select(undefined)} className="size-10 lg:hidden">
                      <ArrowLeft />
                    </IconButton>
                  }
                />
              )}
            </Card>
          )}
        </div>
      )}

      <NewTicketDialog open={creating} onClose={() => setCreating(false)} onCreated={(ticket) => select(ticket.id)} />
    </>
  )
}
