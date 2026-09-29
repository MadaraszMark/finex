import { useId, type ComponentProps, type ReactNode } from 'react'
import { cn } from '../../lib/cn'

export interface TextFieldProps extends ComponentProps<'input'> {
  label: string
  error?: string
  hint?: string
  leftIcon?: ReactNode
  rightSlot?: ReactNode
}

// Címkés beviteli mező hibaüzenettel. A react-hook-form register() eredménye (name, onChange, onBlur, ref)
// egyszerűen ráteríthető, mert a ref React 19-ben sima propként továbbadódik az <input>-nak.
export default function TextField({ label, error, hint, leftIcon, rightSlot, id, className, ...props }: TextFieldProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const messageId = `${inputId}-message`
  const message = error ?? hint

  return (
    <div className={className}>
      <label htmlFor={inputId} className="mb-1.5 block text-sm font-medium text-foreground">
        {label}
      </label>
      <div className="relative">
        {leftIcon && (
          <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4 text-muted-foreground [&_svg]:size-5">
            {leftIcon}
          </span>
        )}
        <input
          id={inputId}
          aria-invalid={error ? true : undefined}
          aria-describedby={message ? messageId : undefined}
          className={cn(
            'h-13 w-full rounded-2xl border border-transparent bg-muted px-4 text-[15px] text-foreground transition duration-200',
            'placeholder:text-muted-foreground/70 focus:border-brand-400 focus:bg-card focus:ring-4 focus:ring-ring/20 focus:outline-none',
            'disabled:cursor-not-allowed disabled:opacity-60',
            leftIcon && 'pl-12',
            rightSlot && 'pr-13',
            error && 'border-danger/60 focus:border-danger focus:ring-danger/15',
          )}
          {...props}
        />
        {rightSlot && <div className="absolute inset-y-0 right-0 flex items-center pr-1.5">{rightSlot}</div>}
      </div>
      {message && (
        <p id={messageId} className={cn('mt-1.5 text-sm', error ? 'text-danger' : 'text-muted-foreground')}>
          {message}
        </p>
      )}
    </div>
  )
}
