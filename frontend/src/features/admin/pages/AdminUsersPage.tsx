import { Search, UsersRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router'
import { getErrorMessage } from '../../../api/client'
import Badge from '../../../components/ui/Badge'
import { Card } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import Pagination from '../../../components/ui/Pagination'
import QueryError from '../../../components/ui/QueryError'
import { Table, Td, Th, Tr } from '../../../components/ui/Table'
import TextField from '../../../components/ui/TextField'
import { cn } from '../../../lib/cn'
import { formatShortDate, nameInitials } from '../../../lib/format'
import { TableRowsSkeleton } from '../components/AdminSkeletons'
import UserDetailsDialog from '../components/UserDetailsDialog'
import { useAdminUsers } from '../queries'

// Felhasználók: keresés névre vagy e-mail címre (a címsorban: ?q=…&page=…)
export default function AdminUsersPage() {
  const [params, setParams] = useSearchParams()
  const search = params.get('q') ?? ''
  const page = Math.max(Number(params.get('page')) || 0, 0)
  const usersQuery = useAdminUsers(search, page)
  const [selectedId, setSelectedId] = useState<number>()
  const [now] = useState(() => new Date())

  // A keresőmező helyben változik, a címsorba (és a keresésbe) 300 ms szünet után kerül
  const [text, setText] = useState(search)
  const [synced, setSynced] = useState(search)
  if (search !== synced) {
    setSynced(search)
    setText(search)
  }

  useEffect(() => {
    if (text.trim() === search) {
      return
    }
    const timer = window.setTimeout(() => setParams(text.trim() ? { q: text.trim() } : {}, { replace: true }), 300)
    return () => window.clearTimeout(timer)
  }, [text, search, setParams])

  const users = usersQuery.data?.content ?? []

  return (
    <>
      <PageHeader eyebrow="Adminisztráció" title="Felhasználók" description="Ügyfelek keresése és kezelése." />

      <Card className="p-5 sm:p-6">
        <TextField
          label="Keresés"
          type="search"
          placeholder="Név vagy e-mail cím"
          leftIcon={<Search />}
          value={text}
          onChange={(event) => setText(event.target.value)}
          className="max-w-md"
        />

        <div className={cn('mt-5 transition-opacity duration-300', usersQuery.isPlaceholderData && 'opacity-50')}>
          {usersQuery.isPending ? (
            <TableRowsSkeleton />
          ) : usersQuery.isError ? (
            <QueryError message={getErrorMessage(usersQuery.error)} retrying={usersQuery.isFetching} onRetry={() => void usersQuery.refetch()} />
          ) : users.length === 0 ? (
            <EmptyState icon={<UsersRound />} title="Nincs találat" description="Próbálj más nevet vagy e-mail címet." />
          ) : (
            <Table caption="Felhasználók">
              <thead>
                <tr>
                  <Th>Név</Th>
                  <Th>E-mail</Th>
                  <Th>Szerepkör</Th>
                  <Th>Állapot</Th>
                  <Th className="text-right">Regisztrált</Th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <Tr key={user.id}>
                    <Td>
                      <button
                        type="button"
                        onClick={() => setSelectedId(user.id)}
                        className="flex items-center gap-3 rounded-xl text-left font-semibold text-foreground transition hover:text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
                      >
                        <span aria-hidden className="flex size-9 items-center justify-center rounded-xl bg-brand-gradient text-xs font-bold text-white">
                          {nameInitials(user.fullName)}
                        </span>
                        {user.fullName}
                      </button>
                    </Td>
                    <Td className="text-muted-foreground">{user.email}</Td>
                    <Td>
                      <Badge tone={user.role === 'ADMIN' ? 'brand' : 'neutral'}>{user.role === 'ADMIN' ? 'Admin' : 'Ügyfél'}</Badge>
                    </Td>
                    <Td>
                      <Badge tone={user.status === 'ACTIVE' ? 'success' : 'danger'}>{user.status === 'ACTIVE' ? 'Aktív' : 'Letiltva'}</Badge>
                    </Td>
                    <Td className="text-right whitespace-nowrap text-muted-foreground">{formatShortDate(new Date(user.createdAt), now)}</Td>
                  </Tr>
                ))}
              </tbody>
            </Table>
          )}
        </div>

        {usersQuery.data && (
          <div className="mt-5">
            <Pagination
              page={page}
              totalPages={usersQuery.data.page.totalPages}
              onPageChange={(next) => setParams(search ? { q: search, page: String(next) } : { page: String(next) })}
            />
          </div>
        )}
      </Card>

      <UserDetailsDialog userId={selectedId} onClose={() => setSelectedId(undefined)} />
    </>
  )
}
