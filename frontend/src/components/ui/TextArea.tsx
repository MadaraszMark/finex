import { useId, type ComponentProps } from 'react'
import { cn } from '../../lib/cn'

export interface TextAreaProps extends ComponentProps<'textarea'> {
  label: string
  error?: string
  hint?: string
  // A címke csak a felolvasónak szól (pl. a csevegő üzenetmezőjénél)
  hideLabel?: boolean
}

// Többsoros mező; a tartalomhoz igazodik (field-sizing), de legfeljebb max-h-ig nő
export default function TextArea({ label, error, hint, hideLabel = false, id, className, ...props }: TextAreaProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const messageId = `${inputId}-message`
  const message = error ?? hint

  return (
    <div className={className}>
      <label htmlFor={inputId} className={cn('mb-1.5 block text-sm font-medium text-foreground', hideLabel && 'sr-only')}>
        {label}
      </label>
      <textarea
        id={inputId}
        aria-invalid={error ? true : undefined}
        aria-describedby={message ? messageId : undefined}
        className={cn(
          'block max-h-60 min-h-28 w-full resize-none rounded-2xl border border-transparent bg-muted px-4 py-3 text-[15px] text-foreground transition duration-200 [field-sizing:content]',
          'placeholder:text-muted-foreground/70 focus:border-brand-400 focus:bg-card focus:ring-4 focus:ring-ring/20 focus:outline-none',
          'disabled:cursor-not-allowed disabled:opacity-60',
          error && 'border-danger/60 focus:border-danger focus:ring-danger/15',
        )}
        {...props}
      />
      {message && (
        <p id={messageId} className={cn('mt-1.5 text-sm', error ? 'text-danger' : 'text-muted-foreground')}>
          {message}
        </p>
      )}
    </div>
  )
}
