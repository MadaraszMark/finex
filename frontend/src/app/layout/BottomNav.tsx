import { LayoutGrid } from 'lucide-react'
import { motion } from 'motion/react'
import { NavLink } from 'react-router'
import { cn } from '../../lib/cn'
import { mobileTabs, type NavItem } from '../navigation'

const tabClass = 'relative flex w-16 flex-col items-center gap-1 pt-1.5 pb-2 text-[11px] font-semibold transition-colors'

function TabLink({ item }: { item: NavItem }) {
  return (
    <NavLink
      to={item.to}
      end={item.end}
      className={({ isActive }) => cn(tabClass, isActive ? 'text-primary' : 'text-muted-foreground hover:text-foreground')}
    >
      {({ isActive }) => (
        <>
          <item.icon className="size-6" />
          <span>{item.label}</span>
          {/* A koncepcióképen az aktív fül alatti pont; animáltan követi a kiválasztott fület */}
          {isActive && (
            <motion.span layoutId="tab-active" className="absolute -bottom-0.5 size-1.5 rounded-full bg-primary" transition={{ type: 'spring', bounce: 0.3, duration: 0.5 }} />
          )}
        </>
      )}
    </NavLink>
  )
}

// A középső, kiemelt fül (Utalás): a leggyakoribb művelet mindig egy érintésre van
function PrimaryTab({ item }: { item: NavItem }) {
  return (
    <NavLink to={item.to} className={cn(tabClass, 'text-muted-foreground')}>
      {({ isActive }) => (
        <>
          <span
            className={cn(
              '-mt-7 flex size-14 items-center justify-center rounded-2xl bg-brand-gradient text-white shadow-glow ring-4 ring-background transition-transform active:scale-95',
              isActive && 'scale-105',
            )}
          >
            <item.icon className="size-6" />
          </span>
          <span className={cn(isActive && 'text-primary')}>{item.label}</span>
        </>
      )}
    </NavLink>
  )
}

// Mobilos alsó menüsor (a koncepciókép alapján); a "Menü" fül az összes többi oldalt tartalmazó lapot nyitja
export default function BottomNav({ onOpenMenu }: { onOpenMenu: () => void }) {
  return (
    <nav aria-label="Alsó menü" className="fixed inset-x-0 bottom-0 z-40 rounded-t-[1.75rem] border-t border-white/70 bg-card/85 pb-safe shadow-[0_-8px_30px_-12px_rgb(28_21_48/0.18)] backdrop-blur-2xl lg:hidden dark:border-white/10">
      <div className="mx-auto flex max-w-md items-end justify-around px-2 pt-1">
        {mobileTabs.left.map((item) => (
          <TabLink key={item.to} item={item} />
        ))}
        <PrimaryTab item={mobileTabs.primary} />
        {mobileTabs.right.map((item) => (
          <TabLink key={item.to} item={item} />
        ))}
        <button type="button" onClick={onOpenMenu} className={cn(tabClass, 'text-muted-foreground hover:text-foreground')}>
          <LayoutGrid className="size-6" />
          <span>Menü</span>
        </button>
      </div>
    </nav>
  )
}
