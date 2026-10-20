import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { closeAccount, deposit, getAccount, getBalanceHistory, getMyAccounts, getStatement, openAccount } from '../../api/accounts'
import { queryKeys } from '../../api/queryKeys'
import type { AccountResponse, DepositRequest } from '../../api/types'
import { invalidateMoneyQueries } from '../../lib/queryClient'

export function useAccounts() {
  return useQuery({ queryKey: queryKeys.accounts, queryFn: getMyAccounts })
}

// A számlaadatlap azonnal megjelenik, ha a számla már szerepel a betöltött listában (utána a háttérben frissül)
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

// Számlanyitás és -zárás után a számlák, a kártyák (a backend kártyát nyit, illetve megszüntet) és az összesítők frissülnek
function useAccountChangeInvalidation() {
  const queryClient = useQueryClient()
  return () => Promise.all([queryKeys.accounts, queryKeys.cards, queryKeys.statistics].map((queryKey) => queryClient.invalidateQueries({ queryKey })))
}

export function useOpenAccount() {
  const invalidate = useAccountChangeInvalidation()
  return useMutation({ mutationFn: openAccount, onSuccess: invalidate })
}

export function useCloseAccount() {
  const invalidate = useAccountChangeInvalidation()
  return useMutation({ mutationFn: closeAccount, onSuccess: invalidate })
}

export function useDeposit() {
  return useMutation({
    mutationFn: ({ accountId, request }: { accountId: number; request: DepositRequest }) => deposit(accountId, request),
    onSuccess: invalidateMoneyQueries,
  })
}
