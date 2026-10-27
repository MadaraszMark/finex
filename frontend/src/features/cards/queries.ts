import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { blockCard, getMyCards, payWithCard, unblockCard, updateCardLimits } from '../../api/cards'
import { queryKeys } from '../../api/queryKeys'
import type { CardPaymentRequest, CardResponse, UpdateCardLimitsRequest } from '../../api/types'
import { invalidateMoneyQueries } from '../../lib/queryClient'

export function useCards() {
  return useQuery({ queryKey: queryKeys.cards, queryFn: getMyCards })
}

// A visszakapott kártyát a gyorsítótárban cseréljük (nincs újratöltés); az összesítő frissül
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

// A fizetés pénzmozgás: az egyenleg, a mai költés és a tételek is frissülnek
export function useCardPayment() {
  return useMutation({
    mutationFn: ({ id, request, idempotencyKey }: { id: number; request: CardPaymentRequest; idempotencyKey: string }) => payWithCard(id, request, idempotencyKey),
    onSuccess: invalidateMoneyQueries,
  })
}
