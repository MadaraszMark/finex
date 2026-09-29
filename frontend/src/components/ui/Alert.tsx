import { CircleAlert, CircleCheck, Info } from 'lucide-react'
import type { ReactNode } from 'react'
import { cn } from '../../lib/cn'

type AlertTone = 'danger' | 'success' | 'info'

const tones: Record<AlertTone, string> = {
  danger: 'border-danger/25 bg-danger/8 text-danger',
  success: 'border-success/25 bg-success/8 text-success',
  info: 'border-brand-300/60 bg-brand-50 text-brand-700 dark:border-brand-800 dark:bg-brand-950/60 dark:text-brand-200',
}

const icons: Record<AlertTone, ReactNode> = {
  danger: <CircleAlert />,
  success: <CircleCheck />,
  info: <Info />,
}

// Űrlap szintű üzenet (pl. a backend hibaüzenete). A felolvasóprogram azonnal felolvassa.
export default function Alert({ tone = 'info', className, children }: { tone?: AlertTone; className?: string; children: ReactNode }) {
  return (
    <div
      role={tone === 'danger' ? 'alert' : 'status'}
      className={cn('flex items-start gap-3 rounded-2xl border px-4 py-3 text-sm font-medium [&_svg]:mt-px [&_svg]:size-5 [&_svg]:shrink-0', tones[tone], className)}
    >
      {icons[tone]}
      <div className="min-w-0">{children}</div>
    </div>
  )
}
