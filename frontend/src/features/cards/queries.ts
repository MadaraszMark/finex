import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { blockCard, getMyCards, payWithCard, unblockCard, updateCardLimits } from '../../api/cards'
import { queryKeys } from '../../api/queryKeys'
import type { CardPaymentRequest, CardResponse, UpdateCardLimitsRequest } from '../../api/types'
import { invalidateMoneyQueries } from '../../lib/queryClient'

export function useCards() {
  return useQuery({ queryKey: queryKeys.cards, queryFn: getMyCards })
}

// A kártyaműveletek a frissített kártyát adják vissza: a gyorsítótárban kicseréljük, így a lista nem töltődik újra.
// Az összesítő (aktív kártyák száma) viszont változhat, ezért az frissül.
function useCardUpdate<TVariables>(mutationFn: (variables: TVariables) => Promise<CardResponse>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSuccess: (card) => {
      queryClient.setQueryData<CardResponse[]>(queryKeys.cards, (cards) => cards?.map((item) => (item.id === card.id ? card : item)))
      return queryClient.invalidateQueries({ queryKey: queryKeys.statistics })
    },
  })
}

export function useBlockCard() {
  return useCardUpdate((id: number) => blockCard(id))
}

export function useUnblockCard() {
  return useCardUpdate((id: number) => unblockCard(id))
}

export function useUpdateCardLimits() {
  return useCardUpdate(({ id, request }: { id: number; request: UpdateCardLimitsRequest }) => updateCardLimits(id, request))
}

// A fizetés pénzmozgás: az egyenleg, a mai költés, a tranzakciók és a statisztikák is frissülnek
export function useCardPayment() {
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: CardPaymentRequest }) => payWithCard(id, request),
    onSuccess: invalidateMoneyQueries,
  })
}
