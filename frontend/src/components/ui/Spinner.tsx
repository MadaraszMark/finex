import { LoaderCircle } from 'lucide-react'
import { cn } from '../../lib/cn'

export default function Spinner({ className, label = 'Betöltés…' }: { className?: string; label?: string }) {
  return <LoaderCircle role="status" aria-label={label} className={cn('size-5 shrink-0 animate-spin', className)} />
}
