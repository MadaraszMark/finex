import { Headset, Lock, SendHorizontal } from 'lucide-react'
import { AnimatePresence, motion } from 'motion/react'
import { useEffect, useRef, useState, type FormEvent, type KeyboardEvent, type ReactNode } from 'react'
import type { SupportTicketResponse, TicketMessageResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import TextArea from '../../../components/ui/TextArea'
import { cn } from '../../../lib/cn'
import { formatDayHeading, formatTime, nameInitials } from '../../../lib/format'
import TicketStatusBadge from './TicketStatusBadge'

interface TicketConversationProps {
  ticket: SupportTicketResponse
  // Ki nézi: ügyfél vagy ügyfélszolgálat (a saját oldal üzenetei jobbra)
  viewer: 'customer' | 'staff'
  onSend: (message: string) => Promise<unknown>
  sending: boolean
  sendError?: string
  // Pl. az admin állapotváltója a fejlécben
  headerActions?: ReactNode
  // Mobilon a "vissza a listához" gomb
  backButton?: ReactNode
}

// A nyitó üzenet is a beszélgetés része (az ügyfél írta a ticket létrehozásakor)
function allMessages(ticket: SupportTicketResponse): TicketMessageResponse[] {
  return [{ id: 0, authorName: ticket.userFullName, staff: false, message: ticket.message, createdAt: ticket.createdAt }, ...ticket.messages]
}

// Csevegő nézet; Ctrl + Enter is küld, új üzenetnél a lista az aljára görget
export default function TicketConversation({ ticket, viewer, onSend, sending, sendError, headerActions, backButton }: TicketConversationProps) {
  const [draft, setDraft] = useState('')
  const listRef = useRef<HTMLDivElement>(null)
  const [now] = useState(() => new Date())
  const messages = allMessages(ticket)
  const resolved = ticket.status === 'RESOLVED'

  useEffect(() => {
    const list = listRef.current
    if (list) {
      list.scrollTo({ top: list.scrollHeight, behavior: 'smooth' })
    }
  }, [messages.length])

  async function send(event?: FormEvent) {
    event?.preventDefault()
    const message = draft.trim()
    if (!message || sending) {
      return
    }
    try {
      await onSend(message)
      setDraft('')
    } catch {
      // A hibaüzenetet a sendError mutatja, a piszkozat megmarad
    }
  }

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
      event.preventDefault()
      void send()
    }
  }

  return (
    <div className="flex h-full min-h-[32rem] flex-col">
      <div className="flex flex-wrap items-start gap-3 border-b border-border pb-4">
        {backButton}
        <div className="min-w-0 flex-1 basis-48">
          <h2 className="text-lg font-bold break-words text-foreground">{ticket.title}</h2>
          <p className="mt-0.5 text-sm text-muted-foreground">
            #{ticket.id}
            {viewer === 'staff' && ` · ${ticket.userFullName} (${ticket.userEmail})`}
          </p>
        </div>
        <div className="flex items-center gap-2 sm:flex-col sm:items-end">
          <TicketStatusBadge status={ticket.status} />
          {headerActions}
        </div>
      </div>

      <div ref={listRef} role="log" aria-label="Beszélgetés" className="min-h-0 flex-1 space-y-3 overflow-y-auto py-5 pr-1 lg:max-h-[55vh]">
        {messages.map((message, index) => {
          const mine = viewer === 'customer' ? !message.staff : message.staff
          const day = new Date(message.createdAt).toDateString()
          const newDay = index === 0 || new Date(messages[index - 1].createdAt).toDateString() !== day
          return (
            <div key={`${message.id}-${index}`}>
              {newDay && (
                <p className="my-3 text-center text-xs font-semibold text-muted-foreground first-letter:uppercase">
                  {formatDayHeading(new Date(message.createdAt), now)}
                </p>
              )}
              <motion.div
                initial={{ opacity: 0, y: 8, scale: 0.98 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                transition={{ type: 'spring', stiffness: 380, damping: 28 }}
                className={cn('flex items-end gap-2.5', mine && 'flex-row-reverse')}
              >
                <span
                  aria-hidden
                  className={cn(
                    'flex size-8 shrink-0 items-center justify-center rounded-xl text-xs font-bold',
                    message.staff ? 'bg-sky-100 text-sky-700 dark:bg-sky-950 dark:text-sky-300' : 'bg-brand-gradient text-white',
                  )}
                >
                  {message.staff ? <Headset className="size-4" /> : nameInitials(message.authorName)}
                </span>
                <div className={cn('max-w-[80%]', mine && 'text-right')}>
                  <div
                    className={cn(
                      'inline-block rounded-3xl px-4 py-2.5 text-left text-[15px] whitespace-pre-wrap break-words',
                      mine ? 'rounded-br-lg bg-brand-gradient text-white shadow-glow' : 'rounded-bl-lg bg-muted text-foreground',
                    )}
                  >
                    {message.message}
                  </div>
                  <p className="mt-1 px-1 text-xs text-muted-foreground">
                    {message.staff ? `${message.authorName} · FineX ügyfélszolgálat` : message.authorName} · {formatTime(message.createdAt)}
                  </p>
                </div>
              </motion.div>
            </div>
          )
        })}
      </div>

      {resolved ? (
        <p className="mt-auto flex items-center gap-2.5 rounded-2xl bg-muted/70 px-4 py-3 text-sm text-muted-foreground">
          <Lock aria-hidden className="size-4 shrink-0" />
          {viewer === 'customer' ? 'A ticket le van zárva. Ha további kérdésed van, nyiss újat.' : 'A ticket le van zárva, új válasz nem küldhető.'}
        </p>
      ) : (
        <form onSubmit={send} className="mt-auto border-t border-border pt-4">
          <AnimatePresence>
            {sendError && (
              <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="mb-3">
                <Alert tone="danger">{sendError}</Alert>
              </motion.div>
            )}
          </AnimatePresence>
          <div className="flex items-end gap-3">
            <TextArea
              label="Üzenet"
              hideLabel
              placeholder={viewer === 'customer' ? 'Írd meg a válaszod…' : 'Válasz az ügyfélnek…'}
              maxLength={5000}
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
              onKeyDown={handleKeyDown}
              className="flex-1 [&_textarea]:min-h-13"
            />
            <Button type="submit" aria-label="Küldés" loading={sending} disabled={!draft.trim()} className="size-13 shrink-0 px-0">
              <SendHorizontal />
            </Button>
          </div>
          <p className="mt-2 text-xs text-muted-foreground">Ctrl + Enter: küldés</p>
        </form>
      )}
    </div>
  )
}
