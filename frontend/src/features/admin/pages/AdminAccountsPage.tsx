import { Banknote, Landmark, ReceiptText, SlidersHorizontal } from 'lucide-react'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse, AccountStatus } from '../../../api/types'
import Badge from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import { Card } from '../../../components/ui/Card'
import ChipGroup from '../../../components/ui/ChipGroup'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import Pagination from '../../../components/ui/Pagination'
import QueryError from '../../../components/ui/QueryError'
import { Table, Td, Th, Tr } from '../../../components/ui/Table'
import { cn } from '../../../lib/cn'
import { accountStatusLabels, formatMoney, formatShortIban } from '../../../lib/format'
import { accountStatusTones } from '../accountStatus'
import AccountStatusDialog from '../components/AccountStatusDialog'
import AccountTransactionsDialog from '../components/AccountTransactionsDialog'
import AdminDepositDialog from '../components/AdminDepositDialog'
import { TableRowsSkeleton } from '../components/AdminSkeletons'
import UserDetailsDialog from '../components/UserDetailsDialog'
import { useAdminAccounts } from '../queries'

type StatusFilter = AccountStatus | 'ALL'

const statusOptions: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: 'Összes' },
  { value: 'ACTIVE', label: 'Aktív' },
  { value: 'FROZEN', label: 'Befagyasztva' },
  { value: 'BLOCKED', label: 'Letiltva' },
  { value: 'CLOSED', label: 'Lezárva' },
]

function parseStatus(value: string | null): AccountStatus | undefined {
  return statusOptions.find((option) => option.value === value && option.value !== 'ALL')?.value as AccountStatus | undefined
}

// Számlák kezelése: állapotszűrés a címsorban (?status=…&page=…), soronként állapotváltás
export default function AdminAccountsPage() {
  const [params, setParams] = useSearchParams()
  const status = parseStatus(params.get('status'))
  const page = Math.max(Number(params.get('page')) || 0, 0)
  const accountsQuery = useAdminAccounts(status, page)
  const [managed, setManaged] = useState<AccountResponse>()
  const [depositing, setDepositing] = useState<AccountResponse>()
  const [inspecting, setInspecting] = useState<AccountResponse>()
  const [userId, setUserId] = useState<number>()

  const accounts = accountsQuery.data?.content ?? []

  return (
    <>
      <PageHeader eyebrow="Adminisztráció" title="Számlák kezelése" description="Befagyasztás, tiltás, feloldás és lezárás." />

      <Card className="p-5 sm:p-6">
        <ChipGroup label="Állapot" value={status ?? 'ALL'} options={statusOptions} onChange={(value) => setParams(value === 'ALL' ? {} : { status: value })} />

        <div className={cn('mt-5 transition-opacity duration-300', accountsQuery.isPlaceholderData && 'opacity-50')}>
          {accountsQuery.isPending ? (
            <TableRowsSkeleton />
          ) : accountsQuery.isError ? (
            <QueryError message={getErrorMessage(accountsQuery.error)} retrying={accountsQuery.isFetching} onRetry={() => void accountsQuery.refetch()} />
          ) : accounts.length === 0 ? (
            <EmptyState icon={<Landmark />} title="Nincs ilyen számla" description="Ebben az állapotban most egy számla sincs." />
          ) : (
            <Table caption="Számlák">
              <thead>
                <tr>
                  <Th>Számla</Th>
                  <Th>Ügyfél</Th>
                  <Th className="text-right">Egyenleg</Th>
                  <Th>Állapot</Th>
                  <Th className="text-right">
                    <span className="sr-only">Műveletek</span>
                  </Th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((account) => (
                  <Tr key={account.id}>
                    <Td>
                      <span className="block font-semibold text-foreground">{account.name}</span>
                      <span className="block font-mono text-xs text-muted-foreground">{formatShortIban(account.accountNumber)}</span>
                    </Td>
                    <Td>
                      <button
                        type="button"
                        onClick={() => setUserId(account.userId)}
                        className="rounded-lg font-semibold text-primary transition hover:underline focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
                      >
                        Ügyfél #{account.userId}
                      </button>
                    </Td>
                    <Td className="text-right font-bold whitespace-nowrap text-foreground tabular-nums">{formatMoney(account.balance, account.currency)}</Td>
                    <Td>
                      <Badge tone={accountStatusTones[account.status]}>{accountStatusLabels[account.status]}</Badge>
                    </Td>
                    <Td className="text-right">
                      <span className="inline-flex flex-wrap justify-end gap-2">
                        <Button variant="ghost" size="sm" leftIcon={<ReceiptText />} onClick={() => setInspecting(account)}>
                          Tételek
                        </Button>
                        {account.status === 'ACTIVE' && (
                          <Button variant="ghost" size="sm" leftIcon={<Banknote />} onClick={() => setDepositing(account)}>
                            Befizetés
                          </Button>
                        )}
                        {account.status !== 'CLOSED' && (
                          <Button variant="outline" size="sm" leftIcon={<SlidersHorizontal />} onClick={() => setManaged(account)}>
                            Kezelés
                          </Button>
                        )}
                      </span>
                    </Td>
                  </Tr>
                ))}
              </tbody>
            </Table>
          )}
        </div>

        {accountsQuery.data && (
          <div className="mt-5">
            <Pagination
              page={page}
              totalPages={accountsQuery.data.page.totalPages}
              onPageChange={(next) => setParams(status ? { status, page: String(next) } : { page: String(next) })}
            />
          </div>
        )}
      </Card>

      <AccountStatusDialog account={managed} onClose={() => setManaged(undefined)} />
      <AdminDepositDialog account={depositing} onClose={() => setDepositing(undefined)} />
      <AccountTransactionsDialog account={inspecting} onClose={() => setInspecting(undefined)} />
      <UserDetailsDialog userId={userId} onClose={() => setUserId(undefined)} />
    </>
  )
}
