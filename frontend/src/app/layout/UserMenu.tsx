import { Headset, LogOut, ShieldCheck } from 'lucide-react'
import { Link } from 'react-router'
import Avatar from '../../components/ui/Avatar'
import Badge from '../../components/ui/Badge'
import Popover from '../../components/ui/Popover'
import { cn } from '../../lib/cn'
import { logout } from '../../session/session'
import { useCurrentUser } from '../../session/useCurrentUser'
import { ThemeSwitcher } from '../../theme/ThemeControls'

const menuLinkClass =
  'flex items-center gap-3 rounded-2xl px-3 py-2.5 text-sm font-semibold text-foreground transition hover:bg-muted [&_svg]:size-5 [&_svg]:text-muted-foreground'

// A fejléc profilmenüje: ki van belépve, gyorslinkek, témaválasztó és kijelentkezés
export default function UserMenu() {
  const { data: user } = useCurrentUser()

  return (
    <Popover
      label="Profilmenü"
      triggerClassName="rounded-2xl transition hover:opacity-90 focus-visible:ring-4 focus-visible:ring-white/40 focus-visible:outline-none"
      trigger={<Avatar lastName={user?.lastName} firstName={user?.firstName} className="ring-2 ring-white/40" />}
      panelClassName="w-76"
    >
      {(close) => (
        <>
          <div className="flex items-center gap-3 px-3 pt-2 pb-3">
            <Avatar lastName={user?.lastName} firstName={user?.firstName} size="lg" />
            <div className="min-w-0">
              <p className="truncate font-bold text-foreground">{user ? `${user.lastName} ${user.firstName}` : '…'}</p>
              <p className="truncate text-sm text-muted-foreground">{user?.email}</p>
              {user?.role === 'ADMIN' && (
                <Badge tone="brand" className="mt-1">
                  Adminisztrátor
                </Badge>
              )}
            </div>
          </div>

          <div className="border-t border-border pt-2">
            <Link to="/profile" onClick={close} className={menuLinkClass}>
              <ShieldCheck />
              Profil és biztonság
            </Link>
            <Link to="/support" onClick={close} className={menuLinkClass}>
              <Headset />
              Ügyfélszolgálat
            </Link>
          </div>

          <div className="mt-2 border-t border-border px-1 pt-3 pb-1">
            <p className="px-2 pb-2 text-xs font-bold tracking-wider text-muted-foreground uppercase">Megjelenés</p>
            <ThemeSwitcher />
          </div>

          <div className="mt-2 border-t border-border pt-2">
            <button type="button" onClick={logout} className={cn(menuLinkClass, 'w-full text-danger [&_svg]:text-danger')}>
              <LogOut />
              Kijelentkezés
            </button>
          </div>
        </>
      )}
    </Popover>
  )
}
