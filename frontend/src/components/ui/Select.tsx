import { ChevronDown } from 'lucide-react'
import { useId, type ComponentProps } from 'react'
import { cn } from '../../lib/cn'

export interface SelectProps extends ComponentProps<'select'> {
  label: string
  error?: string
  hint?: string
}

// Natív <select> (mobilon a rendszer választója nyílik) a TextField-del azonos kinézettel
export default function Select({ label, error, hint, id, className, children, ...props }: SelectProps) {
  const generatedId = useId()
  const selectId = id ?? generatedId
  const messageId = `${selectId}-message`
  const message = error ?? hint

  return (
    <div className={className}>
      <label htmlFor={selectId} className="mb-1.5 block text-sm font-medium text-foreground">
        {label}
      </label>
      <div className="relative">
        <select
          id={selectId}
          aria-invalid={error ? true : undefined}
          aria-describedby={message ? messageId : undefined}
          className={cn(
            'h-13 w-full appearance-none rounded-2xl border border-transparent bg-muted pr-11 pl-4 text-[15px] text-foreground transition duration-200',
            'focus:border-brand-400 focus:bg-card focus:ring-4 focus:ring-ring/20 focus:outline-none',
            'disabled:cursor-not-allowed disabled:opacity-60',
            error && 'border-danger/60 focus:border-danger focus:ring-danger/15',
          )}
          {...props}
        >
          {children}
        </select>
        <ChevronDown aria-hidden className="pointer-events-none absolute top-1/2 right-4 size-5 -translate-y-1/2 text-muted-foreground" />
      </div>
      {message && (
        <p id={messageId} className={cn('mt-1.5 text-sm', error ? 'text-danger' : 'text-muted-foreground')}>
          {message}
        </p>
      )}
    </div>
  )
}
