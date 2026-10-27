import { RefreshCw, TriangleAlert } from 'lucide-react'
import { cn } from '../../lib/cn'
import Button from './Button'

interface QueryErrorProps {
  message: string
  onRetry: () => void
  retrying?: boolean
  className?: string
}

// Sikertelen adatblokk: üzenet és újrapróbálás, az oldal többi része működik tovább
export default function QueryError({ message, onRetry, retrying = false, className }: QueryErrorProps) {
  return (
    <div role="alert" className={cn('flex flex-col items-center gap-3 px-6 py-8 text-center', className)}>
      <span className="flex size-12 items-center justify-center rounded-2xl bg-danger/10 text-danger">
        <TriangleAlert className="size-6" />
      </span>
      <p className="max-w-sm text-sm text-muted-foreground">{message}</p>
      <Button variant="outline" size="sm" leftIcon={<RefreshCw />} loading={retrying} onClick={onRetry}>
        Újrapróbálás
      </Button>
    </div>
  )
}
