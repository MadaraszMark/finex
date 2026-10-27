import { CircleAlert, CircleCheck, Info } from 'lucide-react'
import { useEffect, useRef, type ReactNode } from 'react'
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

// Űrlap szintű üzenet (pl. a backend hibaüzenete), a felolvasó azonnal felolvassa
export default function Alert({ tone = 'info', className, children }: { tone?: AlertTone; className?: string; children: ReactNode }) {
  const alertRef = useRef<HTMLDivElement>(null)

  // Ablakban a hibaüzenet láthatóvá görgetődik (a scroll-mb miatt a ragadós gombsor nem takarja)
  useEffect(() => {
    const alert = alertRef.current
    if (tone === 'danger' && alert?.closest('dialog')) {
      alert.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
    }
  }, [tone])

  return (
    <div
      ref={alertRef}
      role={tone === 'danger' ? 'alert' : 'status'}
      className={cn(
        'flex scroll-mb-28 items-start gap-3 rounded-2xl border px-4 py-3 text-sm font-medium [&_svg]:mt-px [&_svg]:size-5 [&_svg]:shrink-0',
        tones[tone],
        className,
      )}
    >
      {icons[tone]}
      <div className="min-w-0">{children}</div>
    </div>
  )
}
