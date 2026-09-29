import type { ComponentProps, ReactNode } from 'react'
import { cn } from '../../lib/cn'
import { cardSurface } from './cardStyles'

export function Card({ className, ...props }: ComponentProps<'div'>) {
  return <div className={cn(cardSurface, className)} {...props} />
}

interface CardHeaderProps {
  title: string
  description?: string
  action?: ReactNode
  className?: string
}

// Kártya fejléce: cím, rövid leírás és opcionális művelet (pl. "Részletek" link a jobb oldalon)
export function CardHeader({ title, description, action, className }: CardHeaderProps) {
  return (
    <div className={cn('flex items-start justify-between gap-4', className)}>
      <div className="min-w-0">
        <h2 className="text-lg font-bold tracking-tight text-foreground">{title}</h2>
        {description && <p className="mt-0.5 text-sm text-muted-foreground">{description}</p>}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  )
}
