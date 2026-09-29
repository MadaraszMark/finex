import { cn } from '../../lib/cn'

// Betöltés közben a tartalom helyén pulzáló "váz", így az oldal nem ugrál, amikor megérkeznek az adatok
export default function Skeleton({ className }: { className?: string }) {
  return <div aria-hidden className={cn('animate-pulse rounded-2xl bg-muted', className)} />
}
