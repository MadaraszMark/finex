import { skipToken, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { deposit, getMyAccount } from '../api/accounts'
import { getMe } from '../api/auth'
import { getErrorMessage } from '../api/client'
import { getAccountTransactions } from '../api/transactions'
import AccountCard from '../components/AccountCard'
import AppHeader from '../components/AppHeader'
import DepositPanel from '../components/DepositPanel'
import SystemCheck from '../components/SystemCheck'
import TransactionList from '../components/TransactionList'
import { useBackendStatus } from '../hooks/useBackendStatus'
import { queryCheck, type Check } from '../lib/checks'
import { formatIban } from '../lib/format'

const WRITE_CHECK_LABEL = 'Adatbázis – írás'

function depositCheck(mutation: { isPending: boolean; isError: boolean; isSuccess: boolean; error: unknown }): Check {
  if (mutation.isPending) {
    return { label: WRITE_CHECK_LABEL, status: 'pending', detail: 'Befizetés folyamatban…' }
  }
  if (mutation.isError) {
    return { label: WRITE_CHECK_LABEL, status: 'error', detail: getErrorMessage(mutation.error) }
  }
  if (mutation.isSuccess) {
    return { label: WRITE_CHECK_LABEL, status: 'ok', detail: 'A befizetés elmentve, az egyenleg frissült.' }
  }
  return { label: WRITE_CHECK_LABEL, status: 'idle', detail: 'Nyomd meg a „Teszt befizetés” egyik gombját.' }
}

// Ideiglenes főoldal: végigteszteli a frontend → proxy → backend → adatbázis láncot
export default function DashboardPage() {
  const queryClient = useQueryClient()
  const backendStatus = useBackendStatus()
  const meQuery = useQuery({ queryKey: ['me'], queryFn: getMe })
  const accountQuery = useQuery({ queryKey: ['account', 'me'], queryFn: getMyAccount })
  const account = accountQuery.data
  const accountId = account?.id

  const transactionsQuery = useQuery({
    queryKey: ['transactions', accountId],
    queryFn: accountId === undefined ? skipToken : () => getAccountTransactions(accountId),
  })

  // Sikeres befizetés után a számla és a tranzakciók újratöltődnek a backendről
  const depositMutation = useMutation({
    mutationFn: ({ id, amount }: { id: number; amount: number }) =>
      deposit(id, { amount, message: 'Teszt befizetés a frontendről' }),
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: ['account', 'me'] }),
        queryClient.invalidateQueries({ queryKey: ['transactions'] }),
      ]),
  })

  const checks: Check[] = [
    { label: 'Frontend (React + Vite)', status: 'ok', detail: 'Ez az oldal fut, a Tailwind stílusok betöltődtek.' },
    queryCheck('Backend kapcsolat (proxy → :8080)', backendStatus, (status) => `Elérhető, ${status.endpointCount} API-végpont`),
    queryCheck('Bejelentkezés (JWT token)', meQuery, (user) => `Bejelentkezve: ${user.email}`),
    queryCheck('Adatbázis – olvasás', accountQuery, (loaded) => `Számla betöltve: ${formatIban(loaded.accountNumber)}`),
    depositCheck(depositMutation),
  ]

  return (
    <div className="min-h-screen">
      <AppHeader user={meQuery.data} />

      <main className="mx-auto grid max-w-5xl gap-6 px-4 py-8 lg:grid-cols-[1fr_20rem]">
        <div className="space-y-6">
          <div>
            <p className="text-slate-500">Szia,</p>
            <h1 className="text-3xl font-bold text-slate-900">{meQuery.data ? `${meQuery.data.firstName}!` : '…'}</h1>
          </div>

          {accountQuery.isPending && <div className="h-56 animate-pulse rounded-3xl bg-brand-200/60" />}
          {accountQuery.isError && (
            <div role="alert" className="rounded-2xl bg-red-50 p-5 text-sm text-red-700 ring-1 ring-red-200">
              A számla nem tölthető be: {getErrorMessage(accountQuery.error)}
            </div>
          )}
          {account && (
            <>
              <AccountCard account={account} />
              <DepositPanel
                currency={account.currency}
                isPending={depositMutation.isPending}
                errorMessage={depositMutation.isError ? getErrorMessage(depositMutation.error) : undefined}
                onDeposit={(amount) => depositMutation.mutate({ id: account.id, amount })}
              />
            </>
          )}

          {!accountQuery.isError && <TransactionList query={transactionsQuery} />}
        </div>

        <aside className="space-y-4">
          <SystemCheck title="Rendszerteszt" checks={checks} />
          <p className="px-1 text-xs text-slate-500">
            Ideiglenes tesztoldal: ha minden sor zöld, a frontend, a backend és az adatbázis együtt működik.
          </p>
        </aside>
      </main>
    </div>
  )
}
