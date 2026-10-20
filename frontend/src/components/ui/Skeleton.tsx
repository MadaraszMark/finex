import { cn } from '../../lib/cn'

// Betöltés közben a tartalom helyén csillogó "váz" (balról jobbra végigfutó fénysáv), így az oldal nem ugrál,
// amikor megérkeznek az adatok
export default function Skeleton({ className }: { className?: string }) {
  return (
    <div
      aria-hidden
      className={cn(
        'relative overflow-hidden rounded-2xl bg-muted',
        'before:absolute before:inset-0 before:-translate-x-full before:bg-linear-to-r before:from-transparent before:via-white/60 before:to-transparent motion-safe:before:animate-shimmer dark:before:via-white/[0.07]',
        className,
      )}
    />
  )
}
