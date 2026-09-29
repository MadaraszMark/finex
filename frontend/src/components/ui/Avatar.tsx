import { initials } from '../../lib/format'
import { cn } from '../../lib/cn'

type AvatarSize = 'sm' | 'md' | 'lg'

const sizes: Record<AvatarSize, string> = {
  sm: 'size-9 rounded-xl text-xs',
  md: 'size-11 rounded-2xl text-sm',
  lg: 'size-14 rounded-2xl text-lg',
}

// Monogramos profilkép a márka színátmenetével (profilképet a backend nem tárol)
export default function Avatar({
  lastName,
  firstName,
  size = 'md',
  className,
}: {
  lastName?: string
  firstName?: string
  size?: AvatarSize
  className?: string
}) {
  return (
    <span
      aria-hidden
      className={cn('inline-flex shrink-0 items-center justify-center bg-brand-gradient font-bold text-white', sizes[size], className)}
    >
      {initials(lastName, firstName) || '?'}
    </span>
  )
}
