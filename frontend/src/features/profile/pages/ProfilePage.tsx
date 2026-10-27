import { CalendarDays, Mail, Phone, ShieldCheck } from 'lucide-react'
import { useState } from 'react'
import Avatar from '../../../components/ui/Avatar'
import Badge from '../../../components/ui/Badge'
import { Card } from '../../../components/ui/Card'
import PageHeader from '../../../components/ui/PageHeader'
import { formatShortDate } from '../../../lib/format'
import { useCurrentUser } from '../../../session/useCurrentUser'
import AppearanceCard from '../components/AppearanceCard'
import LoginHistoryCard from '../components/LoginHistoryCard'
import PasswordCard from '../components/PasswordCard'
import PersonalDataCard from '../components/PersonalDataCard'
import ProfileSkeleton from '../components/ProfileSkeleton'

// Profil és biztonság: adatok, jelszócsere, belépési napló és megjelenés
export default function ProfilePage() {
  const { data: user } = useCurrentUser()
  const [now] = useState(() => new Date())

  return (
    <>
      <PageHeader eyebrow="Fiók" title="Profil és biztonság" description="Személyes adatok, jelszó, belépések és megjelenés." />

      {!user ? (
        <ProfileSkeleton />
      ) : (
        <>
          <Card className="relative overflow-hidden p-6 sm:p-8">
            <div aria-hidden className="pointer-events-none absolute -top-24 -right-16 size-72 rounded-full bg-brand-400/20 blur-3xl motion-safe:animate-aurora-2 dark:bg-brand-600/20" />
            <div className="relative flex flex-col gap-5 sm:flex-row sm:items-center">
              <Avatar lastName={user.lastName} firstName={user.firstName} size="lg" className="size-20 rounded-[1.75rem] text-2xl shadow-glow" />
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <h2 className="text-2xl font-extrabold tracking-tight text-foreground">
                    {user.lastName} {user.firstName}
                  </h2>
                  <Badge tone={user.role === 'ADMIN' ? 'brand' : 'neutral'}>{user.role === 'ADMIN' ? 'Adminisztrátor' : 'Ügyfél'}</Badge>
                </div>
                <dl className="mt-2 flex flex-wrap gap-x-5 gap-y-1.5 text-sm text-muted-foreground">
                  <div className="flex items-center gap-1.5">
                    <dt className="sr-only">E-mail</dt>
                    <Mail aria-hidden className="size-4" />
                    <dd>{user.email}</dd>
                  </div>
                  {user.phone && (
                    <div className="flex items-center gap-1.5">
                      <dt className="sr-only">Telefon</dt>
                      <Phone aria-hidden className="size-4" />
                      <dd>{user.phone}</dd>
                    </div>
                  )}
                  <div className="flex items-center gap-1.5">
                    <dt className="sr-only">Ügyfél óta</dt>
                    <CalendarDays aria-hidden className="size-4" />
                    <dd>Ügyfél {formatShortDate(new Date(user.createdAt), now)} óta</dd>
                  </div>
                </dl>
              </div>
              <span className="inline-flex items-center gap-2 self-start rounded-full bg-success/12 px-3 py-1.5 text-sm font-semibold text-emerald-700 sm:self-center dark:text-emerald-400">
                <ShieldCheck aria-hidden className="size-4" />
                Fiók aktív
              </span>
            </div>
          </Card>

          <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
            <PersonalDataCard key={user.updatedAt} user={user} />
            <PasswordCard />
          </div>

          <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
            <LoginHistoryCard />
            <AppearanceCard />
          </div>
        </>
      )}
    </>
  )
}
