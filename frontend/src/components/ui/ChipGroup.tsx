import { useRef, type KeyboardEvent } from 'react'
import { cn } from '../../lib/cn'

export interface ChipOption<T extends string> {
  value: T
  label: string
}

interface ChipGroupProps<T extends string> {
  label: string
  value: T | undefined
  options: ChipOption<T>[]
  onChange: (value: T) => void
  className?: string
}

// Választógombok "pirulákként" (pl. időszak: 30 nap, Ez a hónap, …). A SegmentedControl-lal szemben keskeny
// képernyőn több sorba tördelődik. Rádiócsoportként működik: nyilakkal lehet váltani, egyetlen Tab-lépés visz be.
export default function ChipGroup<T extends string>({ label, value, options, onChange, className }: ChipGroupProps<T>) {
  const buttonsRef = useRef<Array<HTMLButtonElement | null>>([])
  const selectedIndex = options.findIndex((option) => option.value === value)

  function handleKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    const forward = event.key === 'ArrowRight' || event.key === 'ArrowDown'
    const backward = event.key === 'ArrowLeft' || event.key === 'ArrowUp'
    if (!forward && !backward) {
      return
    }

    event.preventDefault()
    const nextIndex = (Math.max(selectedIndex, 0) + (forward ? 1 : -1) + options.length) % options.length
    onChange(options[nextIndex].value)
    buttonsRef.current[nextIndex]?.focus()
  }

  return (
    <div role="radiogroup" aria-label={label} onKeyDown={handleKeyDown} className={cn('flex flex-wrap gap-2', className)}>
      {options.map((option, index) => {
        const checked = option.value === value
        return (
          <button
            key={option.value}
            ref={(element) => {
              buttonsRef.current[index] = element
            }}
            type="button"
            role="radio"
            aria-checked={checked}
            tabIndex={checked || (selectedIndex === -1 && index === 0) ? 0 : -1}
            onClick={() => onChange(option.value)}
            className={cn(
              'rounded-full border px-3.5 py-1.5 text-sm font-semibold transition duration-200',
              'focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none',
              checked
                ? 'border-transparent bg-brand-gradient text-white shadow-glow'
                : 'border-border bg-card/80 text-muted-foreground hover:border-brand-300 hover:text-foreground',
            )}
          >
            {option.label}
          </button>
        )
      })}
    </div>
  )
}
