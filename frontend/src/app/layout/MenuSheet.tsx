import { LogOut } from 'lucide-react'
import { NavLink } from 'react-router'
import Avatar from '../../components/ui/Avatar'
import Button from '../../components/ui/Button'
import Dialog from '../../components/ui/Dialog'
import { cn } from '../../lib/cn'
import { logout } from '../../session/session'
import { useCurrentUser } from '../../session/useCurrentUser'
import { ThemeSwitcher } from '../../theme/ThemeControls'
import { visibleSections } from '../navigation'

// Mobilon az alsó "Menü" fül lapja: minden oldal csempékben, témaválasztó és kijelentkezés
export default function MenuSheet({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { data: user } = useCurrentUser()
  const sections = visibleSections(user?.role === 'ADMIN')

  return (
    <Dialog open={open} onClose={onClose} title="Menü" variant="sheet">
      <div className="flex items-center gap-3 rounded-2xl bg-muted/70 p-3">
        <Avatar lastName={user?.lastName} firstName={user?.firstName} />
        <div className="min-w-0">
          <p className="truncate font-bold">{user ? `${user.lastName} ${user.firstName}` : '…'}</p>
          <p className="truncate text-sm text-muted-foreground">{user?.email}</p>
        </div>
      </div>

      <nav aria-label="Összes oldal" className="mt-2 max-h-[50dvh] overflow-y-auto [scrollbar-width:thin]">
        {sections.map((section) => (
          <div key={section.title} className="mt-4">
            <p className="px-1 pb-2 text-[11px] font-bold tracking-wider text-muted-foreground uppercase">{section.title}</p>
            <div className="grid grid-cols-2 gap-2">
              {section.items.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.end}
                  onClick={onClose}
                  className={({ isActive }) =>
                    cn(
                      'flex min-h-14 items-center gap-2.5 rounded-2xl border px-3 py-2.5 text-sm leading-tight font-semibold transition',
                      isActive
                        ? 'border-transparent bg-brand-gradient text-white shadow-glow'
                        : 'border-border bg-card text-foreground hover:border-brand-300',
                    )
                  }
                >
                  <item.icon className="size-5 shrink-0" />
                  <span>{item.label}</span>
                </NavLink>
              ))}
            </div>
          </div>
        ))}
      </nav>

      <div className="mt-5 border-t border-border pt-4">
        <p className="px-1 pb-2 text-[11px] font-bold tracking-wider text-muted-foreground uppercase">Megjelenés</p>
        <ThemeSwitcher />
      </div>

      <Button
        variant="ghost"
        fullWidth
        leftIcon={<LogOut />}
        className="mt-4 text-danger hover:bg-danger/10 hover:text-danger"
        onClick={() => {
          onClose()
          logout()
        }}
      >
        Kijelentkezés
      </Button>
    </Dialog>
  )
}
