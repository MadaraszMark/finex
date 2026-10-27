import { useId } from 'react'
import { cn } from '../../lib/cn'
import Spinner from './Spinner'

interface SwitchProps {
  checked: boolean
  onCheckedChange: (checked: boolean) => void
  label: string
  description?: string
  disabled?: boolean
  loading?: boolean
  // danger: bekapcsolva piros (pl. Kártya tiltása)
  tone?: 'default' | 'danger'
  className?: string
}

// Kétállású kapcsoló (role="switch"), billentyűzettel és felolvasóval is kezelhető
export default function Switch({ checked, onCheckedChange, label, description, disabled, loading, tone = 'default', className }: SwitchProps) {
  const labelId = useId()
  const descriptionId = useId()

  return (
    <div className={cn('flex items-center justify-between gap-4', className)}>
      <div className="min-w-0">
        <p id={labelId} className="font-semibold text-foreground">
          {label}
        </p>
        {description && (
          <p id={descriptionId} className="mt-0.5 text-sm text-muted-foreground">
            {description}
          </p>
        )}
      </div>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        aria-labelledby={labelId}
        aria-describedby={description ? descriptionId : undefined}
        aria-busy={loading || undefined}
        disabled={disabled || loading}
        onClick={() => onCheckedChange(!checked)}
        className={cn(
          'relative inline-flex h-8 w-14 shrink-0 items-center rounded-full p-1 transition-colors duration-200',
          'focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-60',
          checked ? (tone === 'danger' ? 'bg-danger-solid' : 'bg-brand-gradient') : 'bg-muted-foreground/30',
        )}
      >
        <span
          className={cn(
            'flex size-6 items-center justify-center rounded-full bg-white shadow-md transition-transform duration-200',
            checked && 'translate-x-6',
          )}
        >
          {loading && <Spinner label="Mentés…" className="size-3.5 text-brand-600" />}
        </span>
      </button>
    </div>
  )
}
