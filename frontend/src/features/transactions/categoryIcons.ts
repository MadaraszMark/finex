import {
  ArrowDownLeft,
  ArrowDownToLine,
  ArrowUpRight,
  Baby,
  Banknote,
  BookOpen,
  Briefcase,
  Bus,
  Car,
  CircleEllipsis,
  Coffee,
  CreditCard,
  Dog,
  Dumbbell,
  Film,
  Fuel,
  Gamepad2,
  Gift,
  GraduationCap,
  HeartPulse,
  House,
  Music,
  PiggyBank,
  Plane,
  Shirt,
  ShoppingBag,
  ShoppingCart,
  Smartphone,
  Sparkles,
  Stethoscope,
  Utensils,
  Wifi,
  Wrench,
  Zap,
  type LucideIcon,
} from 'lucide-react'
import { createElement, type ReactElement } from 'react'
import type { TransactionType } from '../../api/types'

// A kategóriaikon a törzsadatban lucide-név; csak az itt felsoroltak kerülnek a csomagba
const categoryIcons: Record<string, LucideIcon> = {
  'shopping-cart': ShoppingCart,
  utensils: Utensils,
  bus: Bus,
  zap: Zap,
  house: House,
  'heart-pulse': HeartPulse,
  film: Film,
  'shopping-bag': ShoppingBag,
  plane: Plane,
  banknote: Banknote,
  'piggy-bank': PiggyBank,
  gift: Gift,
  'circle-ellipsis': CircleEllipsis,
  car: Car,
  fuel: Fuel,
  dumbbell: Dumbbell,
  'graduation-cap': GraduationCap,
  baby: Baby,
  dog: Dog,
  shirt: Shirt,
  coffee: Coffee,
  smartphone: Smartphone,
  wifi: Wifi,
  'book-open': BookOpen,
  music: Music,
  'gamepad-2': Gamepad2,
  stethoscope: Stethoscope,
  briefcase: Briefcase,
  wrench: Wrench,
  sparkles: Sparkles,
}

// Az ikonválasztó kínálata (a backend legfeljebb 20 karakteres nevet fogad el)
export const categoryIconNames = Object.keys(categoryIcons)

// Az ikonok neve a felolvasóknak és a súgónak (admin ikonválasztó)
export const categoryIconLabels: Record<string, string> = {
  'shopping-cart': 'Bevásárlókocsi',
  utensils: 'Evőeszköz',
  bus: 'Busz',
  zap: 'Villám',
  house: 'Ház',
  'heart-pulse': 'Szívverés',
  film: 'Film',
  'shopping-bag': 'Bevásárlótáska',
  plane: 'Repülő',
  banknote: 'Bankjegy',
  'piggy-bank': 'Malacpersely',
  gift: 'Ajándék',
  'circle-ellipsis': 'Egyéb',
  car: 'Autó',
  fuel: 'Üzemanyag',
  dumbbell: 'Súlyzó',
  'graduation-cap': 'Diplomasapka',
  baby: 'Baba',
  dog: 'Kutya',
  shirt: 'Ing',
  coffee: 'Kávé',
  smartphone: 'Okostelefon',
  wifi: 'Wifi',
  'book-open': 'Könyv',
  music: 'Zene',
  'gamepad-2': 'Játékvezérlő',
  stethoscope: 'Sztetoszkóp',
  briefcase: 'Aktatáska',
  wrench: 'Csavarkulcs',
  sparkles: 'Csillogás',
}

const typeIcons: Record<TransactionType, LucideIcon> = {
  INCOME: ArrowDownToLine,
  OUTCOME: CreditCard,
  TRANSFER_IN: ArrowDownLeft,
  TRANSFER_OUT: ArrowUpRight,
}

// Kész elemet ad vissza, nem komponenst, így renderelés közben nem jön létre új komponens
export function transactionIcon(type: TransactionType, categoryIconName: string | null | undefined, className: string): ReactElement {
  const icon = (categoryIconName && categoryIcons[categoryIconName]) || typeIcons[type]
  return createElement(icon, { 'aria-hidden': true, className })
}

// Egy kategóriaikon kész elemként (ismeretlen névnél a "többi" ikon)
export function categoryIcon(name: string | null | undefined, className: string): ReactElement {
  return createElement((name && categoryIcons[name]) || CircleEllipsis, { 'aria-hidden': true, className })
}
