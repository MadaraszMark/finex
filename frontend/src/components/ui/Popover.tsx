import { AnimatePresence, motion } from 'motion/react'
import { useEffect, useId, useRef, useState, type ReactNode } from 'react'
import { cn } from '../../lib/cn'

interface PopoverProps {
  // A nyitógomb tartalma és a felolvasóprogramoknak szóló címkéje
  trigger: ReactNode
  label: string
  // A panel tartalma; a close() hívással bezárható (pl. egy linkre kattintás után)
  children: (close: () => void) => ReactNode
  align?: 'start' | 'end'
  triggerClassName?: string
  panelClassName?: string
}

// Lenyíló panel (pl. a profilmenü). Kívülre kattintva vagy Escape-re bezárul, és a fókusz visszakerül a gombra.
export default function Popover({ trigger, label, children, align = 'end', triggerClassName, panelClassName }: PopoverProps) {
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const triggerRef = useRef<HTMLButtonElement>(null)
  const panelId = useId()

  useEffect(() => {
    if (!open) {
      return
    }

    function handlePointerDown(event: PointerEvent) {
      if (!rootRef.current?.contains(event.target as Node)) {
        setOpen(false)
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setOpen(false)
        triggerRef.current?.focus()
      }
    }

    document.addEventListener('pointerdown', handlePointerDown)
    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('pointerdown', handlePointerDown)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open])

  const close = () => setOpen(false)

  return (
    <div ref={rootRef} className="relative">
      <button
        ref={triggerRef}
        type="button"
        aria-label={label}
        aria-expanded={open}
        aria-controls={open ? panelId : undefined}
        onClick={() => setOpen((current) => !current)}
        className={triggerClassName}
      >
        {trigger}
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            id={panelId}
            initial={{ opacity: 0, y: -6, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -6, scale: 0.97 }}
            transition={{ duration: 0.16, ease: 'easeOut' }}
            className={cn(
              'absolute top-full z-50 mt-2 rounded-3xl border border-border bg-card/95 p-2 text-foreground shadow-xl backdrop-blur-xl',
              align === 'end' ? 'right-0 origin-top-right' : 'left-0 origin-top-left',
              panelClassName,
            )}
          >
            {children(close)}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
