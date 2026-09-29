import type { ReactNode } from 'react'
import { cn } from '../../lib/cn'

interface EmptyStateProps {
  icon: ReactNode
  title: string
  description?: string
  action?: ReactNode
  className?: string
}

// Üres lista vagy még nem elérhető tartalom helyén: ikon, rövid magyarázat és egy lehetséges következő lépés
export default function EmptyState({ icon, title, description, action, className }: EmptyStateProps) {
  return (
    <div className={cn('flex flex-col items-center px-6 py-12 text-center', className)}>
      <span className="flex size-16 items-center justify-center rounded-3xl bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300 [&_svg]:size-7">
        {icon}
      </span>
      <h3 className="mt-5 text-lg font-bold text-foreground">{title}</h3>
      {description && <p className="mt-1.5 max-w-sm text-sm text-muted-foreground">{description}</p>}
      {action && <div className="mt-6">{action}</div>}
    </div>
  )
}
