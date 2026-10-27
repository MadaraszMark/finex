import { Ban, CreditCard, Landmark, LockOpen, PiggyBank, ScrollText, ShieldCheck, UserRound, UserRoundCog } from 'lucide-react'
import { useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { UserResponse, UserRole, UserStatus } from '../../../api/types'
import Avatar from '../../../components/ui/Avatar'
import Badge from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import ButtonLink from '../../../components/ui/ButtonLink'
import ConfirmDialog from '../../../components/ui/ConfirmDialog'
import Dialog from '../../../components/ui/Dialog'
import QueryError from '../../../components/ui/QueryError'
import Skeleton from '../../../components/ui/Skeleton'
import { accountStatusLabels, cardStatusLabels, formatMaskedCard, formatMoney, formatShortDate, formatShortIban } from '../../../lib/format'
import { accountStatusTones } from '../accountStatus'
import { useAdminUser, useUpdateUserRole, useUpdateUserStatus } from '../queries'

type PendingChange = { kind: 'status'; value: UserStatus } | { kind: 'role'; value: UserRole }

// Ügyfél adatlapja; saját fiókját az admin nem tilthatja le, a szerepkörét sem módosíthatja
export default function UserDetailsDialog({ userId, onClose }: { userId: number | undefined; onClose: () => void }) {
  const [shownId, setShownId] = useState(userId)
  if (userId !== undefined && userId !== shownId) {
    setShownId(userId)
  }

  const userQuery = useAdminUser(shownId)
  const updateStatus = useUpdateUserStatus()
  const updateRole = useUpdateUserRole()
  const [pending, setPending] = useState<PendingChange>()
  const [now] = useState(() => new Date())

  const details = userQuery.data
  const user: UserResponse | undefined = details?.user

  function confirm() {
    if (!user || !pending) {
      return
    }
    const options = {
      onSuccess: () => {
        setPending(undefined)
        toast.success('Mentettük a módosítást', { description: `${user.lastName} ${user.firstName}` })
      },
      onError: (error: unknown) => {
        setPending(undefined)
        toast.error(getErrorMessage(error))
      },
    }
    if (pending.kind === 'status') {
      updateStatus.mutate({ id: user.id, status: pending.value }, options)
    } else {
      updateRole.mutate({ id: user.id, role: pending.value }, options)
    }
  }

  return (
    <Dialog open={userId !== undefined} onClose={onClose} icon={<UserRound />} title="Ügyfél adatlapja" size="lg">
      {userQuery.isPending ? (
        <div className="space-y-4">
          <Skeleton className="h-20" />
          <Skeleton className="h-40" />
        </div>
      ) : userQuery.isError || !details || !user ? (
        <QueryError message={getErrorMessage(userQuery.error)} retrying={userQuery.isFetching} onRetry={() => void userQuery.refetch()} />
      ) : (
        <div className="space-y-6">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
            <Avatar lastName={user.lastName} firstName={user.firstName} size="lg" />
            <div className="min-w-0 flex-1">
              <p className="text-lg font-bold text-foreground">
                {user.lastName} {user.firstName}
              </p>
              <p className="truncate text-sm text-muted-foreground">
                {user.email}
                {user.phone && ` · ${user.phone}`} · regisztrált: {formatShortDate(new Date(user.createdAt), now)}
              </p>
              <div className="mt-2 flex flex-wrap gap-2">
                <Badge tone={user.role === 'ADMIN' ? 'brand' : 'neutral'}>{user.role === 'ADMIN' ? 'Adminisztrátor' : 'Ügyfél'}</Badge>
                <Badge tone={user.status === 'ACTIVE' ? 'success' : 'danger'}>{user.status === 'ACTIVE' ? 'Aktív' : 'Letiltva'}</Badge>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            {user.status === 'ACTIVE' ? (
              <Button variant="outline" size="sm" leftIcon={<Ban />} className="hover:border-danger/60 hover:text-danger" onClick={() => setPending({ kind: 'status', value: 'BLOCKED' })}>
                Letiltás
              </Button>
            ) : (
              <Button variant="outline" size="sm" leftIcon={<LockOpen />} onClick={() => setPending({ kind: 'status', value: 'ACTIVE' })}>
                Feloldás
              </Button>
            )}
            {user.role === 'USER' ? (
              <Button variant="outline" size="sm" leftIcon={<ShieldCheck />} onClick={() => setPending({ kind: 'role', value: 'ADMIN' })}>
                Adminisztrátorrá tétel
              </Button>
            ) : (
              <Button variant="outline" size="sm" leftIcon={<UserRoundCog />} onClick={() => setPending({ kind: 'role', value: 'USER' })}>
                Visszaállítás ügyfélre
              </Button>
            )}
            <ButtonLink to={`/admin/login-logs?user=${user.id}`} variant="outline" size="sm" leftIcon={<ScrollText />} onClick={onClose}>
              Belépési napló
            </ButtonLink>
          </div>

          <section aria-labelledby="user-accounts">
            <h3 id="user-accounts" className="mb-2 flex items-center gap-2 text-sm font-bold text-foreground">
              <Landmark aria-hidden className="size-4 text-primary" /> Számlák ({details.accounts.length})
            </h3>
            <ul className="divide-y divide-border rounded-2xl border border-border">
              {details.accounts.map((account) => (
                <li key={account.id} className="flex flex-wrap items-center justify-between gap-2 px-4 py-3 text-sm">
                  <span className="min-w-0">
                    <span className="block font-semibold text-foreground">{account.name}</span>
                    <span className="block font-mono text-xs text-muted-foreground">{formatShortIban(account.accountNumber)}</span>
                  </span>
                  <span className="flex items-center gap-3">
                    <span className="font-bold text-foreground tabular-nums">{formatMoney(account.balance, account.currency)}</span>
                    <Badge tone={accountStatusTones[account.status]}>{accountStatusLabels[account.status]}</Badge>
                  </span>
                </li>
              ))}
              {details.accounts.length === 0 && <li className="px-4 py-3 text-sm text-muted-foreground">Nincs számlája.</li>}
            </ul>
          </section>

          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            <section aria-labelledby="user-cards">
              <h3 id="user-cards" className="mb-2 flex items-center gap-2 text-sm font-bold text-foreground">
                <CreditCard aria-hidden className="size-4 text-primary" /> Kártyák ({details.cards.length})
              </h3>
              <ul className="space-y-2">
                {details.cards.map((card) => (
                  <li key={card.id} className="flex items-center justify-between gap-2 rounded-2xl bg-muted/70 px-4 py-3 text-sm">
                    <span className="font-mono text-foreground">{formatMaskedCard(card.maskedNumber)}</span>
                    <Badge tone={card.status === 'ACTIVE' ? 'success' : card.status === 'BLOCKED' ? 'danger' : 'neutral'}>{cardStatusLabels[card.status]}</Badge>
                  </li>
                ))}
                {details.cards.length === 0 && <li className="text-sm text-muted-foreground">Nincs kártyája.</li>}
              </ul>
            </section>
            <section aria-labelledby="user-savings">
              <h3 id="user-savings" className="mb-2 flex items-center gap-2 text-sm font-bold text-foreground">
                <PiggyBank aria-hidden className="size-4 text-primary" /> Megtakarítások ({details.savingsAccounts.length})
              </h3>
              <ul className="space-y-2">
                {details.savingsAccounts.map((savings) => (
                  <li key={savings.id} className="flex items-center justify-between gap-2 rounded-2xl bg-muted/70 px-4 py-3 text-sm">
                    <span className="truncate font-semibold text-foreground">{savings.name}</span>
                    <span className="shrink-0 font-bold text-foreground tabular-nums">{formatMoney(savings.balance, savings.currency)}</span>
                  </li>
                ))}
                {details.savingsAccounts.length === 0 && <li className="text-sm text-muted-foreground">Nincs megtakarítása.</li>}
              </ul>
            </section>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={pending !== undefined}
        onClose={() => setPending(undefined)}
        onConfirm={confirm}
        loading={updateStatus.isPending || updateRole.isPending}
        tone={pending?.kind === 'status' && pending.value === 'BLOCKED' ? 'danger' : 'primary'}
        title={
          pending?.kind === 'status'
            ? pending.value === 'BLOCKED'
              ? 'Letiltod az ügyfelet?'
              : 'Feloldod az ügyfél letiltását?'
            : pending?.value === 'ADMIN'
              ? 'Adminisztrátorrá teszed?'
              : 'Visszaállítod ügyfélre?'
        }
        description={
          pending?.kind === 'status'
            ? pending.value === 'BLOCKED'
              ? 'A letiltott ügyfél nem tud belépni, amíg fel nem oldod.'
              : 'Az ügyfél újra be tud lépni.'
            : pending?.value === 'ADMIN'
              ? 'Az adminisztrátor minden ügyfél adatait látja és módosíthatja.'
              : 'Az admin felülethez többé nem fér hozzá.'
        }
        confirmLabel="Megerősítés"
      />
    </Dialog>
  )
}
