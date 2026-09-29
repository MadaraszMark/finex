import { X } from 'lucide-react'
import { useEffect, useId, useRef, type ReactNode } from 'react'
import { cn } from '../../lib/cn'
import IconButton from './IconButton'

interface DialogProps {
  open: boolean
  onClose: () => void
  title: string
  description?: string
  children: ReactNode
  // "sheet": mobilon alulról felcsúszó lap, nagyobb képernyőn középre igazított ablak
  variant?: 'center' | 'sheet'
  className?: string
}

// Felugró ablak a natív <dialog> elemre építve: a böngésző kezeli a fókuszcsapdát, az Escape billentyűt
// és a háttér letiltását. A ki- és beúszó animáció az index.css .finex-dialog szabályaiban van.
export default function Dialog({ open, onClose, title, description, children, variant = 'center', className }: DialogProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const titleId = useId()
  const descriptionId = useId()

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) {
      return
    }
    if (open && !dialog.open) {
      dialog.showModal()
    }
    if (!open && dialog.open) {
      dialog.close()
    }
  }, [open])

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby={titleId}
      aria-describedby={description ? descriptionId : undefined}
      onClose={onClose}
      // A dialóguson kívülre (a sötétített háttérre) kattintva bezárul
      onClick={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
      className={cn(
        'finex-dialog max-h-[90dvh] bg-card text-foreground shadow-2xl',
        variant === 'sheet'
          ? 'finex-sheet mx-0 mt-auto mb-0 w-full max-w-none rounded-t-[2rem] sm:m-auto sm:w-[calc(100%-2rem)] sm:max-w-lg sm:rounded-3xl'
          : 'w-[calc(100%-2rem)] max-w-lg rounded-3xl',
        className,
      )}
    >
      <div className={cn('p-6', variant === 'sheet' && 'pb-[calc(1.5rem+env(safe-area-inset-bottom))] sm:pb-6')}>
        {variant === 'sheet' && <div aria-hidden className="mx-auto -mt-2 mb-4 h-1.5 w-12 rounded-full bg-border sm:hidden" />}
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <h2 id={titleId} className="text-xl font-bold tracking-tight">
              {title}
            </h2>
            {description && (
              <p id={descriptionId} className="mt-1 text-sm text-muted-foreground">
                {description}
              </p>
            )}
          </div>
          <IconButton label="Bezárás" onClick={onClose} className="-mt-1 -mr-2 size-10 border-transparent bg-transparent">
            <X />
          </IconButton>
        </div>
        <div className="mt-5">{children}</div>
      </div>
    </dialog>
  )
}
