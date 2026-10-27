import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { closeAccount, getAccount, getBalanceHistory, getMyAccounts, getStatement, openAccount } from '../../api/accounts'
import { queryKeys } from '../../api/queryKeys'
import { exchange } from '../../api/transactions'
import type { AccountResponse } from '../../api/types'
import { invalidateMoneyQueries } from '../../lib/queryClient'

export function useAccounts() {
  return useQuery({ queryKey: queryKeys.accounts, queryFn: getMyAccounts })
}

// A már betöltött listából azonnal megjelenik, utána a háttérben frissül
export function useAccount(id: number) {
  const queryClient = useQueryClient()
  return useQuery({
    queryKey: queryKeys.account(id),
    queryFn: () => getAccount(id),
    enabled: Number.isSafeInteger(id) && id > 0,
    initialData: () => queryClient.getQueryData<AccountResponse[]>(queryKeys.accounts)?.find((account) => account.id === id),
    initialDataUpdatedAt: () => queryClient.getQueryState(queryKeys.accounts)?.dataUpdatedAt,
  })
}

// Időszakváltáskor az előző kivonat látszik (halványítva), amíg az új megérkezik
export function useStatement(accountId: number, from: string, to: string) {
  return useQuery({
    queryKey: queryKeys.statement(accountId, from, to),
    queryFn: () => getStatement(accountId, from, to),
    placeholderData: keepPreviousData,
  })
}

export function useBalanceHistory(accountId: number, from: string, to: string) {
  return useQuery({
    queryKey: queryKeys.balanceHistory(accountId, from, to),
    queryFn: () => getBalanceHistory(accountId, from, to),
    placeholderData: keepPreviousData,
  })
}

// Számlanyitás és -zárás a kártyákat is érinti (a backend kártyát nyit, illetve megszüntet)
function useAccountChangeInvalidation() {
  const queryClient = useQueryClient()
  // Számlazáráskor a róla induló rendszeres átutalások is leállnak, ezért azok is frissülnek
  return () => Promise.all([queryKeys.accounts, queryKeys.cards, queryKeys.statistics, queryKeys.standingOrders].map((queryKey) => queryClient.invalidateQueries({ queryKey })))
}

export function useOpenAccount() {
  const invalidate = useAccountChangeInvalidation()
  return useMutation({ mutationFn: openAccount, onSuccess: invalidate })
}

export function useCloseAccount() {
  const invalidate = useAccountChangeInvalidation()
  return useMutation({ mutationFn: closeAccount, onSuccess: invalidate })
}

// Devizaváltás két saját számla között: mindkét számla egyenlege és a tételek is változnak
export function useExchange() {
  return useMutation({ mutationFn: exchange, onSuccess: invalidateMoneyQueries })
}
