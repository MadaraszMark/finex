import { LogOut } from 'lucide-react'
import { motion } from 'motion/react'
import { useEffect, useRef, useState, type FocusEvent, type PointerEvent } from 'react'
import { Link, NavLink, useLocation } from 'react-router'
import Logo, { LogoMark } from '../../components/brand/Logo'
import Avatar from '../../components/ui/Avatar'
import IconButton from '../../components/ui/IconButton'
import { cn } from '../../lib/cn'
import { SHY } from '../../lib/format'
import { logout } from '../../session/session'
import { useCurrentUser } from '../../session/useCurrentUser'
import { visibleSections, type NavItem } from '../navigation'

// Ikonsávnál a név fix pozíciójú buborékban jelenik meg, hogy a görgethető menü ne vágja le
function SidebarLink({ item }: { item: NavItem }) {
  const [tipTop, setTipTop] = useState<number>()
  const label = item.label.replaceAll(SHY, '')

  function showTip(event: PointerEvent<HTMLAnchorElement> | FocusEvent<HTMLAnchorElement>) {
    const rect = event.currentTarget.getBoundingClientRect()
    setTipTop(rect.top + rect.height / 2)
  }

  return (
    <NavLink
      to={item.to}
      end={item.end}
      aria-label={label}
      onPointerEnter={showTip}
      onPointerLeave={() => setTipTop(undefined)}
      onFocus={showTip}
      onBlur={() => setTipTop(undefined)}
      className={({ isActive }) =>
        cn(
          'relative flex items-center gap-3 rounded-2xl px-3 py-2.5 text-sm font-semibold transition-colors lg:justify-center xl:justify-start',
          'focus-visible:ring-4 focus-visible:ring-white/30 focus-visible:outline-none',
          isActive ? 'text-white' : 'text-white/65 hover:bg-white/10 hover:text-white',
        )
      }
    >
      {({ isActive }) => (
        <>
          {isActive && (
            <motion.span
              layoutId="sidebar-active"
              className="absolute inset-0 rounded-2xl bg-brand-gradient shadow-glow"
              transition={{ type: 'spring', bounce: 0.15, duration: 0.5 }}
            />
          )}
          <item.icon className="relative size-5 shrink-0" />
          <span className="relative hidden min-w-0 truncate xl:block">{item.label}</span>
          {tipTop !== undefined && (
            <span
              aria-hidden
              style={{ top: tipTop }}
              className="pointer-events-none fixed left-[5.75rem] z-50 -translate-y-1/2 rounded-xl border border-white/10 bg-[#1d1036] px-3 py-1.5 text-xs font-semibold whitespace-nowrap text-white shadow-[0_12px_30px_-10px_rgb(24_10_50/0.6)] xl:hidden"
            >
              {label}
            </span>
          )}
        </>
      )}
    </NavLink>
  )
}

// Asztali oldalsáv: menüszakaszok, alul a belépett felhasználó
export default function Sidebar() {
  const { data: user } = useCurrentUser()
  const isAdmin = user?.role === 'ADMIN'
  const sections = visibleSections(isAdmin)
  const navRef = useRef<HTMLElement>(null)
  const { pathname } = useLocation()

  // Görget az aktív menüpontig; az admin pontok csak a szerepkör betöltése után jelennek meg
  useEffect(() => {
    navRef.current?.querySelector('[aria-current="page"]')?.scrollIntoView({ block: 'nearest' })
  }, [pathname, isAdmin])

  return (
    <aside className="sticky top-0 isolate z-40 hidden h-dvh w-20 shrink-0 flex-col overflow-hidden border-r border-white/5 bg-[linear-gradient(180deg,#1d1036_0%,#150b29_55%,#100820_100%)] text-white lg:flex xl:w-72">
      <div aria-hidden className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-32 -left-28 size-80 rounded-full bg-brand-600/30 blur-3xl will-change-transform motion-safe:animate-aurora-1" />
        <div className="absolute -right-36 -bottom-40 size-80 rounded-full bg-fuchsia-600/15 blur-3xl will-change-transform motion-safe:animate-aurora-3" />
      </div>

      <div className="flex justify-center px-3 pt-7 pb-3 xl:block xl:px-6">
        <Link to="/" aria-label="FineX főoldal" className="inline-block rounded-2xl focus-visible:ring-4 focus-visible:ring-white/30 focus-visible:outline-none">
          <LogoMark tone="light" className="xl:hidden" />
          <Logo tone="light" className="hidden xl:inline-flex" />
        </Link>
      </div>

      <nav ref={navRef} aria-label="Főmenü" className="flex-1 overflow-y-auto px-3 pb-6 [scrollbar-color:rgb(255_255_255/0.15)_transparent] [scrollbar-width:none] xl:px-4 xl:[scrollbar-width:thin]">
        {sections.map((section, index) => (
          <div key={section.title} className="mt-6 first:mt-3">
            {index > 0 && <div aria-hidden className="mx-auto mb-4 h-px w-8 bg-white/15 xl:hidden" />}
            <p className="px-3 pb-2 text-[11px] font-bold tracking-wider text-white/40 uppercase lg:sr-only xl:not-sr-only">{section.title}</p>
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

      <div className="p-3 xl:p-4">
        <div className="flex flex-col items-center gap-2 rounded-2xl border border-white/10 bg-white/[0.06] p-2 backdrop-blur-md xl:flex-row xl:gap-3 xl:p-3">
          <Avatar lastName={user?.lastName} firstName={user?.firstName} size="sm" />
          <div className="hidden min-w-0 flex-1 xl:block">
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
