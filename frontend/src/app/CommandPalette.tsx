import { CornerDownLeft, LogOut, Moon, Search, Send, Sun, Wallet, type LucideIcon } from 'lucide-react'
import { AnimatePresence, motion } from 'motion/react'
import { useId, useMemo, useState, type KeyboardEvent } from 'react'
import { useNavigate } from 'react-router'
import ModalShell from '../components/ui/ModalShell'
import { useAccounts } from '../features/accounts/queries'
import { useBeneficiaries } from '../features/beneficiaries/queries'
import { cn } from '../lib/cn'
import { formatMoney, formatShortIban, SHY } from '../lib/format'
import { logout } from '../session/session'
import { useCurrentUser } from '../session/useCurrentUser'
import { changeTheme } from '../theme/themeTransition'
import { useTheme } from '../theme/useTheme'
import { visibleSections } from './navigation'

interface CommandItem {
  id: string
  group: string
  label: string
  hint?: string
  icon: LucideIcon
  keywords?: string
  run: () => void
}

// Kereséshez: kisbetűs, ékezet és feltételes elválasztójel nélküli alak ("kedvezmenyezett" is megtalálja)
function normalize(text: string): string {
  return text.replaceAll(SHY, '').normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase()
}

// Gyorskereső (Ctrl/⌘ + K): oldalak, gyakori műveletek, számlák és kedvezményezettek egy helyen.
// Billentyűzettel teljesen kezelhető (↑/↓ léptetés, Enter megnyitás, Esc bezárás), a felolvasónak combobox-listbox.
export default function CommandPalette({ open, onClose }: { open: boolean; onClose: () => void }) {
  return <AnimatePresence>{open && <CommandPanel onClose={onClose} />}</AnimatePresence>
}

function CommandPanel({ onClose }: { onClose: () => void }) {
  const navigate = useNavigate()
  const { data: user } = useCurrentUser()
  const { data: accounts = [] } = useAccounts()
  const { data: beneficiaries = [] } = useBeneficiaries()
  const { resolved } = useTheme()
  const [query, setQuery] = useState('')
  const [activeIndex, setActiveIndex] = useState(0)
  const titleId = useId()
  const listId = useId()

  const items = useMemo<CommandItem[]>(
    () => [
      {
        id: 'action:transfer',
        group: 'Műveletek',
        label: 'Új utalás',
        icon: Send,
        keywords: 'pénzküldés átutalás',
        run: () => navigate('/transfer'),
      },
      {
        id: 'action:theme',
        group: 'Műveletek',
        label: resolved === 'dark' ? 'Váltás világos módra' : 'Váltás sötét módra',
        icon: resolved === 'dark' ? Sun : Moon,
        keywords: 'téma megjelenés',
        run: () => changeTheme(resolved === 'dark' ? 'light' : 'dark'),
      },
      { id: 'action:logout', group: 'Műveletek', label: 'Kijelentkezés', icon: LogOut, keywords: 'kilépés', run: logout },
      ...visibleSections(user?.role === 'ADMIN').flatMap((section) =>
        section.items.map((item) => ({
          id: `page:${item.to}`,
          group: 'Oldalak',
          label: item.label.replaceAll(SHY, ''),
          hint: section.title,
          icon: item.icon,
          run: () => navigate(item.to),
        })),
      ),
      ...accounts
        .filter((account) => account.status !== 'CLOSED')
        .map((account) => ({
          id: `account:${account.id}`,
          group: 'Számlák',
          label: account.name,
          hint: formatMoney(account.balance, account.currency),
          icon: Wallet,
          keywords: account.accountNumber,
          run: () => navigate(`/accounts/${account.id}`),
        })),
      ...beneficiaries.map((beneficiary) => ({
        id: `beneficiary:${beneficiary.id}`,
        group: 'Utalás kedvezményezettnek',
        label: beneficiary.name,
        hint: formatShortIban(beneficiary.accountNumber),
        icon: Send,
        keywords: `${beneficiary.accountNumber} ${beneficiary.note ?? ''}`,
        run: () => navigate(`/transfer?${new URLSearchParams({ to: beneficiary.accountNumber, name: beneficiary.name })}`),
      })),
    ],
    [navigate, resolved, user?.role, accounts, beneficiaries],
  )

  const results = useMemo(() => {
    const term = normalize(query.trim())
    const matching = term ? items.filter((item) => normalize(`${item.label} ${item.hint ?? ''} ${item.keywords ?? ''} ${item.group}`).includes(term)) : items
    return matching.slice(0, 40)
  }, [items, query])

  function run(item: CommandItem) {
    onClose()
    item.run()
  }

  function handleKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault()
      if (results.length === 0) {
        return
      }
      const next = (activeIndex + (event.key === 'ArrowDown' ? 1 : -1) + results.length) % results.length
      setActiveIndex(next)
      document.getElementById(`${listId}-${next}`)?.scrollIntoView({ block: 'nearest' })
    } else if (event.key === 'Enter') {
      event.preventDefault()
      const item = results[activeIndex]
      if (item) {
        run(item)
      }
    }
  }

  return (
    <ModalShell onClose={onClose} labelledBy={titleId} placement="top">
      <motion.div
        initial={{ opacity: 0, scale: 0.96, y: -14 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.98, y: -8 }}
        transition={{ type: 'spring', stiffness: 420, damping: 32 }}
        className="pointer-events-auto flex max-h-[72vh] w-full max-w-xl flex-col overflow-hidden rounded-[1.75rem] border border-white/60 bg-card/95 shadow-[0_40px_90px_-30px_rgb(24_10_50/0.6)] backdrop-blur-2xl dark:border-white/10"
      >
        <h2 id={titleId} className="sr-only">
          Gyorskereső
        </h2>
        <div className="flex items-center gap-3 border-b border-border px-5">
          <Search aria-hidden className="size-5 shrink-0 text-primary" />
          <input
            role="combobox"
            aria-expanded
            aria-controls={listId}
            aria-autocomplete="list"
            aria-activedescendant={results[activeIndex] ? `${listId}-${activeIndex}` : undefined}
            aria-label="Keresés oldalak, műveletek, számlák és kedvezményezettek között"
            autoFocus
            value={query}
            onChange={(event) => {
              setQuery(event.target.value)
              setActiveIndex(0)
            }}
            onKeyDown={handleKeyDown}
            placeholder="Keress oldalt, számlát, címzettet…"
            className="h-16 min-w-0 flex-1 bg-transparent text-base text-foreground outline-none placeholder:text-muted-foreground"
          />
          <kbd className="hidden rounded-lg border border-border px-2 py-1 text-xs font-semibold text-muted-foreground sm:block">Esc</kbd>
        </div>

        <ul id={listId} role="listbox" aria-label="Találatok" className="min-h-0 flex-1 overflow-y-auto p-2">
          {results.length === 0 && (
            <li role="presentation" className="px-4 py-10 text-center text-sm text-muted-foreground">
              Nincs találat erre: „{query}”
            </li>
          )}
          {results.map((item, index) => {
            const showGroup = index === 0 || results[index - 1].group !== item.group
            const active = index === activeIndex
            return (
              <li key={item.id} role="presentation">
                {showGroup && <p className="px-3 pt-3 pb-1.5 text-[11px] font-bold tracking-wider text-muted-foreground uppercase">{item.group}</p>}
                <div
                  id={`${listId}-${index}`}
                  role="option"
                  aria-selected={active}
                  onMouseMove={() => setActiveIndex(index)}
                  onClick={() => run(item)}
                  className={cn(
                    'flex cursor-pointer items-center gap-3 rounded-2xl px-3 py-2.5 transition-colors',
                    active ? 'bg-brand-gradient text-white shadow-glow' : 'text-foreground',
                  )}
                >
                  <span
                    className={cn(
                      'flex size-9 shrink-0 items-center justify-center rounded-xl',
                      active ? 'bg-white/20' : 'bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300',
                    )}
                  >
                    <item.icon aria-hidden className="size-[18px]" />
                  </span>
                  <span className="min-w-0 flex-1 truncate font-semibold">{item.label}</span>
                  {item.hint && <span className={cn('shrink-0 truncate text-sm', active ? 'text-white/80' : 'text-muted-foreground')}>{item.hint}</span>}
                  {active && <CornerDownLeft aria-hidden className="size-4 shrink-0" />}
                </div>
              </li>
            )
          })}
        </ul>

        <div className="hidden items-center gap-4 border-t border-border px-5 py-3 text-xs text-muted-foreground sm:flex">
          <span className="flex items-center gap-1.5">
            <kbd className="rounded-md border border-border px-1.5 py-0.5 font-semibold">↑</kbd>
            <kbd className="rounded-md border border-border px-1.5 py-0.5 font-semibold">↓</kbd>
            léptetés
          </span>
          <span className="flex items-center gap-1.5">
            <kbd className="rounded-md border border-border px-1.5 py-0.5 font-semibold">Enter</kbd>
            megnyitás
          </span>
        </div>
      </motion.div>
    </ModalShell>
  )
}
