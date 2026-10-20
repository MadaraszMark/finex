import { Check } from 'lucide-react'
import { cn } from '../../../lib/cn'

// A varázsló lépései: a kész lépések pipát kapnak, a köztük lévő vonal a haladással töltődik
export default function Stepper({ steps, current }: { steps: readonly string[]; current: number }) {
  return (
    <ol className="flex items-center gap-2">
      {steps.map((title, index) => {
        const done = index < current
        const active = index === current
        return (
          <li key={title} aria-current={active ? 'step' : undefined} className={cn('flex items-center gap-2', index < steps.length - 1 && 'flex-1')}>
            <span
              className={cn(
                'flex size-8 shrink-0 items-center justify-center rounded-full text-sm font-bold transition duration-300',
                done || active ? 'bg-brand-gradient text-white' : 'bg-muted text-muted-foreground',
                active && 'shadow-glow ring-4 ring-brand-300/40 dark:ring-brand-700/40',
              )}
            >
              {done ? <Check aria-hidden className="size-4" /> : index + 1}
            </span>
            <span className={cn('hidden text-sm font-semibold sm:inline', active ? 'text-foreground' : 'text-muted-foreground')}>
              {title}
              {done && <span className="sr-only"> (kész)</span>}
            </span>
            {index < steps.length - 1 && (
              <span aria-hidden className="h-0.5 min-w-4 flex-1 overflow-hidden rounded-full bg-border">
                <span className={cn('block h-full bg-brand-gradient transition-[width] duration-500', done ? 'w-full' : 'w-0')} />
              </span>
            )}
          </li>
        )
      })}
    </ol>
  )
}
