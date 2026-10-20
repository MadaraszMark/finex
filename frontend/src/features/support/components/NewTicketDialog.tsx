import { zodResolver } from '@hookform/resolvers/zod'
import { MessageCirclePlus } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { z } from 'zod'
import { getErrorMessage } from '../../../api/client'
import type { SupportTicketResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import TextArea from '../../../components/ui/TextArea'
import TextField from '../../../components/ui/TextField'
import { useCreateTicket } from '../queries'

// A backend szabályai (CreateSupportTicketRequest)
const ticketSchema = z.object({
  title: z.string().trim().min(1, 'Add meg röviden, miről szól a kérdésed.').max(200, 'Legfeljebb 200 karakter lehet.'),
  message: z.string().trim().min(1, 'Írd le a kérdésed.').max(5000, 'Legfeljebb 5000 karakter lehet.'),
})

type TicketForm = z.infer<typeof ticketSchema>

interface NewTicketDialogProps {
  open: boolean
  onClose: () => void
  onCreated: (ticket: SupportTicketResponse) => void
}

// Új kérdés az ügyfélszolgálatnak (egyszerre egy megválaszolatlan ticket lehet, ezt a backend ellenőrzi)
export default function NewTicketDialog({ open, onClose, onCreated }: NewTicketDialogProps) {
  const createTicket = useCreateTicket()
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TicketForm>({ resolver: zodResolver(ticketSchema), defaultValues: { title: '', message: '' } })

  function close() {
    onClose()
    reset()
    createTicket.reset()
  }

  function onSubmit(values: TicketForm) {
    createTicket.mutate(values, {
      onSuccess: (ticket) => {
        close()
        onCreated(ticket)
        toast.success('Elküldtük a kérdésed', { description: 'Az ügyfélszolgálat válaszáról értesítést kapsz.' })
      },
    })
  }

  return (
    <Dialog open={open} onClose={close} icon={<MessageCirclePlus />} title="Új kérdés" description="Írd le, miben segíthetünk – általában egy munkanapon belül válaszolunk.">
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <TextField label="Tárgy" placeholder="pl. Nem érkezett meg egy utalásom" maxLength={200} error={errors.title?.message} {...register('title')} />
        <TextArea label="Üzenet" placeholder="Mikor, melyik számláról, mekkora összeg…" maxLength={5000} error={errors.message?.message} {...register('message')} />

        {createTicket.isError && <Alert tone="danger">{getErrorMessage(createTicket.error)}</Alert>}

        <DialogActions>
          <Button variant="ghost" onClick={close}>
            Mégse
          </Button>
          <Button type="submit" loading={createTicket.isPending}>
            Küldés
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
