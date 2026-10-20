import { ArrowLeftRight, Headset, Megaphone, PiggyBank, ShieldAlert, type LucideIcon } from 'lucide-react'
import type { NotificationType } from '../../api/types'

interface NotificationMeta {
  label: string
  icon: LucideIcon
  // Az ikon jelvényének színe (a típus egy pillantásra felismerhető)
  badge: string
  // Hová visz a kattintás (ahol az esemény részletei vannak)
  link?: string
}

export const notificationMeta: Record<NotificationType, NotificationMeta> = {
  TRANSACTION: {
    label: 'Pénzmozgás',
    icon: ArrowLeftRight,
    badge: 'bg-success/12 text-emerald-700 dark:text-emerald-400',
    link: '/transactions',
  },
  SECURITY: {
    label: 'Biztonság',
    icon: ShieldAlert,
    badge: 'bg-danger/10 text-danger',
    link: '/profile',
  },
  SAVINGS: {
    label: 'Megtakarítás',
    icon: PiggyBank,
    badge: 'bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300',
    link: '/savings',
  },
  SUPPORT: {
    label: 'Ügyfélszolgálat',
    icon: Headset,
    badge: 'bg-sky-100 text-sky-700 dark:bg-sky-950 dark:text-sky-300',
    link: '/support',
  },
  SYSTEM: {
    label: 'Rendszerüzenet',
    icon: Megaphone,
    badge: 'bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300',
  },
}
