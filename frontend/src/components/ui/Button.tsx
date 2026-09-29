import type { ComponentProps, ReactNode } from 'react'
import { buttonStyles, type ButtonStyleOptions } from './buttonStyles'
import Spinner from './Spinner'

interface ButtonProps extends ComponentProps<'button'>, Omit<ButtonStyleOptions, 'className'> {
  loading?: boolean
  leftIcon?: ReactNode
  rightIcon?: ReactNode
}

// Betöltés közben a gomb letiltott és pörgő ikont mutat, így nem lehet kétszer elküldeni az űrlapot
export default function Button({
  variant,
  size,
  fullWidth,
  loading = false,
  leftIcon,
  rightIcon,
  className,
  children,
  disabled,
  type = 'button',
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      className={buttonStyles({ variant, size, fullWidth, className })}
      {...props}
    >
      {loading ? <Spinner /> : leftIcon}
      {children}
      {/* A jobb oldali ikon (pl. nyíl) egérrel fölé állva kicsit előrecsúszik */}
      {!loading && rightIcon && <span className="flex transition-transform duration-200 group-hover:translate-x-1">{rightIcon}</span>}
    </button>
  )
}
