import { LogOut } from 'lucide-react'
import { motion } from 'motion/react'
import { Link, NavLink } from 'react-router'
import Logo from '../../components/brand/Logo'
import Avatar from '../../components/ui/Avatar'
import IconButton from '../../components/ui/IconButton'
import { cn } from '../../lib/cn'
import { logout } from '../../session/session'
import { useCurrentUser } from '../../session/useCurrentUser'
import { visibleSections, type NavItem } from '../navigation'

function SidebarLink({ item }: { item: NavItem }) {
  return (
    <NavLink
      to={item.to}
      end={item.end}
      className={({ isActive }) =>
        cn(
          'relative flex items-center gap-3 rounded-2xl px-3 py-2.5 text-sm font-semibold transition-colors',
          'focus-visible:ring-4 focus-visible:ring-white/30 focus-visible:outline-none',
          isActive ? 'text-white' : 'text-white/65 hover:bg-white/10 hover:text-white',
        )
      }
    >
      {({ isActive }) => (
        <>
          {/* Az aktív menüpont háttere animáltan "átcsúszik" az új menüpontra */}
          {isActive && (
            <motion.span
              layoutId="sidebar-active"
              className="absolute inset-0 rounded-2xl bg-brand-gradient shadow-glow"
              transition={{ type: 'spring', bounce: 0.15, duration: 0.5 }}
            />
          )}
          <item.icon className="relative size-5 shrink-0" />
          <span className="relative truncate">{item.label}</span>
        </>
      )}
    </NavLink>
  )
}

// Asztali nézet oldalsávja mély ibolya háttéren (világos módban is), lassan úszó fényfoltokkal: menüszakaszok
// (adminnak az adminisztrációs rész is), alul a belépett felhasználó
export default function Sidebar() {
  const { data: user } = useCurrentUser()
  const sections = visibleSections(user?.role === 'ADMIN')

  return (
    <aside className="sticky top-0 isolate hidden h-dvh w-72 shrink-0 flex-col overflow-hidden border-r border-white/5 bg-[linear-gradient(180deg,#1d1036_0%,#150b29_55%,#100820_100%)] text-white lg:flex">
      <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-32 -left-28 size-80 rounded-full bg-brand-600/30 blur-3xl will-change-transform motion-safe:animate-aurora-1" />
        <div className="absolute -right-36 -bottom-40 size-80 rounded-full bg-fuchsia-600/15 blur-3xl will-change-transform motion-safe:animate-aurora-3" />
      </div>

      <div className="px-6 pt-7 pb-3">
        <Link to="/" aria-label="FineX főoldal" className="inline-block rounded-2xl focus-visible:ring-4 focus-visible:ring-white/30 focus-visible:outline-none">
          <Logo tone="light" />
        </Link>
      </div>

      <nav aria-label="Főmenü" className="flex-1 overflow-y-auto px-4 pb-6 [scrollbar-color:rgb(255_255_255/0.15)_transparent]">
        {sections.map((section) => (
          <div key={section.title} className="mt-6 first:mt-3">
            <p className="px-3 pb-2 text-[11px] font-bold tracking-wider text-white/40 uppercase">{section.title}</p>
            <ul className="space-y-1">
              {section.items.map((item) => (
                <li key={item.to}>
                  <SidebarLink item={item} />
                </li>
              ))}
            </ul>
          </div>
        ))}
      </nav>

      <div className="p-4">
        <div className="flex items-center gap-3 rounded-2xl border border-white/10 bg-white/[0.06] p-3 backdrop-blur-md">
          <Avatar lastName={user?.lastName} firstName={user?.firstName} size="sm" />
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-bold">{user ? `${user.lastName} ${user.firstName}` : '…'}</p>
            <p className="truncate text-xs text-white/55">{user?.email}</p>
          </div>
          <IconButton
            label="Kijelentkezés"
            tone="light"
            onClick={logout}
            className="size-9 border-transparent bg-transparent text-white/60 hover:bg-white/10 hover:text-rose-300"
          >
            <LogOut />
          </IconButton>
        </div>
      </div>
    </aside>
  )
}
