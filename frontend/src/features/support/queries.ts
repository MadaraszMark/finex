import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '../../api/queryKeys'
import { addTicketMessage, createTicket, getMyTickets, getTicket } from '../../api/support'

export function useMyTickets() {
  return useQuery({ queryKey: queryKeys.tickets, queryFn: () => getMyTickets(), select: (page) => page.content })
}

// A megnyitott ticket félpercenként frissül, így az ügyfélszolgálat válasza magától megjelenik (lezárás után már nem)
export function useTicket(id: number | undefined) {
  return useQuery({
    queryKey: queryKeys.ticket(id ?? 0),
    queryFn: () => getTicket(id ?? 0),
    enabled: id !== undefined,
    refetchInterval: (query) => (query.state.data?.status === 'RESOLVED' ? false : 30_000),
  })
}

export function useCreateTicket() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createTicket,
    onSuccess: (ticket) => {
      queryClient.setQueryData(queryKeys.ticket(ticket.id), ticket)
      return queryClient.invalidateQueries({ queryKey: queryKeys.tickets, exact: true })
    },
  })
}

// Az elküldött üzenet után a backend a teljes ticketet adja vissza: ezt tesszük a gyorsítótárba
export function useSendTicketMessage() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, message }: { id: number; message: string }) => addTicketMessage(id, message),
    onSuccess: (ticket) => {
      queryClient.setQueryData(queryKeys.ticket(ticket.id), ticket)
      return queryClient.invalidateQueries({ queryKey: queryKeys.tickets, exact: true })
    },
  })
}
