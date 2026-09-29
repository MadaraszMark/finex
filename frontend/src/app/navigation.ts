import {
  ArrowLeftRight,
  Bell,
  CalendarClock,
  ChartPie,
  CreditCard,
  Headset,
  House,
  Landmark,
  LayoutDashboard,
  MessagesSquare,
  PiggyBank,
  ScrollText,
  Send,
  ShieldCheck,
  Tags,
  UsersRound,
  Wallet,
  type LucideIcon,
} from 'lucide-react'
import { SHY } from '../lib/format'

// Az alkalmazás menüje egy helyen: ebből épül az oldalsáv (asztali nézet), az alsó menüsor
// és a mobilos "Menü" lap. Az útvonalak a backend erőforrásainak nevét követik (/accounts, /cards, …).

export interface NavItem {
  label: string
  to: string
  icon: LucideIcon
  // Csak pontos egyezésnél aktív (pl. a "/" főoldal ne legyen aktív minden aloldalon)
  end?: boolean
}

export interface NavSection {
  title: string
  items: NavItem[]
  adminOnly?: boolean
}

export const navSections: NavSection[] = [
  {
    title: 'Áttekintés',
    items: [
      { label: 'Főoldal', to: '/', icon: House, end: true },
      { label: 'Számlák', to: '/accounts', icon: Wallet },
      { label: 'Kártyák', to: '/cards', icon: CreditCard },
    ],
  },
  {
    title: 'Pénzmozgás',
    items: [
      { label: 'Utalás', to: '/transfer', icon: Send },
      { label: 'Tranzakciók', to: '/transactions', icon: ArrowLeftRight },
      { label: `Kedvezmé${SHY}nyezettek`, to: '/beneficiaries', icon: UsersRound },
      { label: 'Rendszeres átutalások', to: '/standing-orders', icon: CalendarClock },
    ],
  },
  {
    title: 'Tervezés',
    items: [
      { label: `Megtaka${SHY}rítások`, to: '/savings', icon: PiggyBank },
      { label: 'Statisztika', to: '/statistics', icon: ChartPie },
    ],
  },
  {
    title: 'Fiók',
    items: [
      { label: 'Értesítések', to: '/notifications', icon: Bell },
      { label: `Ügyfél${SHY}szolgálat`, to: '/support', icon: Headset },
      { label: 'Profil és biztonság', to: '/profile', icon: ShieldCheck },
    ],
  },
  {
    title: 'Adminisztráció',
    adminOnly: true,
    items: [
      { label: 'Admin áttekintés', to: '/admin', icon: LayoutDashboard, end: true },
      { label: `Felhasz${SHY}nálók`, to: '/admin/users', icon: UsersRound },
      { label: 'Számlák kezelése', to: '/admin/accounts', icon: Landmark },
      { label: 'Ticketek', to: '/admin/support-tickets', icon: MessagesSquare },
      { label: 'Belépési napló', to: '/admin/login-logs', icon: ScrollText },
      { label: 'Kategóriák', to: '/admin/categories', icon: Tags },
    ],
  },
]

// Mobilon az alsó menüsor fülei (a középső "Utalás" kiemelt gomb); a többi oldal a "Menü" lapon érhető el
export const mobileTabs: { left: NavItem[]; primary: NavItem; right: NavItem[] } = {
  left: [
    { label: 'Főoldal', to: '/', icon: House, end: true },
    { label: 'Kártyák', to: '/cards', icon: CreditCard },
  ],
  primary: { label: 'Utalás', to: '/transfer', icon: Send },
  right: [{ label: 'Tranzakciók', to: '/transactions', icon: ArrowLeftRight }],
}

// A felhasználó szerepköréhez illő menüszakaszok
export function visibleSections(isAdmin: boolean): NavSection[] {
  return navSections.filter((section) => !section.adminOnly || isAdmin)
}

export function findNavItem(path: string): NavItem | undefined {
  return navSections.flatMap((section) => section.items).find((item) => item.to === path)
}
