import { motion } from 'motion/react'
import { useId, useRef, type KeyboardEvent, type ReactNode } from 'react'
import { cn } from '../../lib/cn'

export interface SegmentedOption<T extends string> {
  value: T
  label: string
  icon?: ReactNode
}

interface SegmentedControlProps<T extends string> {
  label: string
  value: T
  options: SegmentedOption<T>[]
  onChange: (value: T) => void
  className?: string
}

// Rádiógomb-csoport "pirula" stílusban: a kijelölés animáltan csúszik át (motion layoutId).
// Billentyűzettel a nyilakkal lehet váltani, a csoportba egyetlen Tab-lépés visz be.
export default function SegmentedControl<T extends string>({ label, value, options, onChange, className }: SegmentedControlProps<T>) {
  const indicatorId = useId()
  const buttonsRef = useRef<Array<HTMLButtonElement | null>>([])

  function handleKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    const forward = event.key === 'ArrowRight' || event.key === 'ArrowDown'
    const backward = event.key === 'ArrowLeft' || event.key === 'ArrowUp'
    if (!forward && !backward) {
      return
    }

    event.preventDefault()
    const currentIndex = options.findIndex((option) => option.value === value)
    const nextIndex = (currentIndex + (forward ? 1 : -1) + options.length) % options.length
    onChange(options[nextIndex].value)
    buttonsRef.current[nextIndex]?.focus()
  }

  return (
    <div role="radiogroup" aria-label={label} onKeyDown={handleKeyDown} className={cn('flex rounded-2xl bg-muted p-1', className)}>
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
            tabIndex={checked ? 0 : -1}
            onClick={() => onChange(option.value)}
            className={cn(
              'relative flex flex-1 items-center justify-center rounded-xl px-2 py-2 text-sm font-semibold transition-colors',
              'focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none',
              checked ? 'text-foreground' : 'text-muted-foreground hover:text-foreground',
            )}
          >
            {checked && (
              <motion.span
                layoutId={indicatorId}
                className="absolute inset-0 rounded-xl bg-card shadow-sm"
                transition={{ type: 'spring', bounce: 0.2, duration: 0.4 }}
              />
            )}
            <span className="relative flex items-center gap-1.5 [&_svg]:size-4">
              {option.icon}
              {option.label}
            </span>
          </button>
        )
      })}
    </div>
  )
}
