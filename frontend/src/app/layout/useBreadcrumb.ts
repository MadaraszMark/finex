import { matchPath, useLocation } from 'react-router'
import { navSections, type NavItem } from '../navigation'

export interface Breadcrumb {
  section: string
  label: string
}

// Hol jár a felhasználó a menü szerint (pl. "Pénzmozgás / Tranzakciók"): a leghosszabb illeszkedő menüpont nyer
export function useBreadcrumb(): Breadcrumb | null {
  const { pathname } = useLocation()

  let best: { item: NavItem; section: string } | null = null
  for (const section of navSections) {
    for (const item of section.items) {
      const matches = matchPath({ path: item.to, end: item.end ?? false }, pathname) !== null
      if (matches && (!best || item.to.length > best.item.to.length)) {
        best = { item, section: section.title }
      }
    }
  }

  return best ? { section: best.section, label: best.item.label } : null
}
