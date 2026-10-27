import { motion } from 'motion/react'
import { useLayoutEffect, useRef, type ReactNode } from 'react'
import { cn } from '../../lib/cn'
import { isOverToast, raiseToasts } from '../../lib/topLayer'

interface ModalShellProps {
  onClose: () => void
  labelledBy: string
  describedBy?: string
  // responsive: mobilon alul, nagyobb képernyőn középen; top: mindig felül (gyorskereső)
  placement?: 'responsive' | 'top'
  children: ReactNode
}

// Natív <dialog> (fókuszcsapda, inert háttér); a ::backdrop nem animálható, ezért saját háttér van
export default function ModalShell({ onClose, labelledBy, describedBy, placement = 'responsive', children }: ModalShellProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)

  useLayoutEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) {
      return
    }
    dialog.showModal()
    // Az értesítő buborékok az ablak fölé kerülnek (lib/topLayer)
    raiseToasts()
    const root = document.documentElement
    const previousOverflow = root.style.overflow
    root.style.overflow = 'hidden'
    return () => {
      root.style.overflow = previousOverflow
      if (dialog.open) {
        dialog.close()
      }
    }
  }, [])

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby={labelledBy}
      aria-describedby={describedBy}
      onCancel={(event) => {
        event.preventDefault()
        onClose()
      }}
      className="fixed inset-0 m-0 size-full max-h-none max-w-none overflow-hidden border-0 bg-transparent p-0 text-foreground backdrop:bg-transparent"
    >
      <motion.div
        aria-hidden
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        transition={{ duration: 0.25 }}
        onClick={(event) => {
          if (!isOverToast(event.clientX, event.clientY)) {
            onClose()
          }
        }}
        className="absolute inset-0 bg-[radial-gradient(120%_80%_at_50%_0%,rgb(124_69_209/0.28),transparent_60%),linear-gradient(rgb(13_10_22/0.5),rgb(13_10_22/0.5))] backdrop-blur-md"
      />
      <div className={cn('pointer-events-none absolute inset-0 flex justify-center', placement === 'top' ? 'items-start px-4 pt-[9vh]' : 'items-end sm:items-center sm:p-6')}>
        {children}
      </div>
    </dialog>
  )
}
