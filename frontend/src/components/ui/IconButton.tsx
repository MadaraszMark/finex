import type { ComponentProps } from 'react'
import { iconButtonStyles, type IconButtonTone } from './buttonStyles'

interface IconButtonProps extends ComponentProps<'button'> {
  // Csak ikon látszik, ezért a felolvasóprogramoknak kötelező a szöveges címke
  label: string
  tone?: IconButtonTone
}

export default function IconButton({ label, tone, className, children, type = 'button', ...props }: IconButtonProps) {
  return (
    <button type={type} aria-label={label} title={label} className={iconButtonStyles(tone, className)} {...props}>
      {children}
    </button>
  )
}
