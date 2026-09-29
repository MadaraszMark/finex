import type { ReactNode } from 'react'
import { Link, type LinkProps } from 'react-router'
import { buttonStyles, type ButtonStyleOptions } from './buttonStyles'

interface ButtonLinkProps extends LinkProps, Omit<ButtonStyleOptions, 'className'> {
  leftIcon?: ReactNode
  rightIcon?: ReactNode
  className?: string
}

// Gombnak kinéző navigációs link (pl. "Regisztráció →"): szemantikailag link marad, mert oldalt vált
export default function ButtonLink({ variant, size, fullWidth, leftIcon, rightIcon, className, children, ...props }: ButtonLinkProps) {
  return (
    <Link className={buttonStyles({ variant, size, fullWidth, className })} {...props}>
      {leftIcon}
      {children}
      {rightIcon && <span className="flex transition-transform duration-200 group-hover:translate-x-1">{rightIcon}</span>}
    </Link>
  )
}
