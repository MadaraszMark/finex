import { X } from 'lucide-react'
import { AnimatePresence, motion, useDragControls } from 'motion/react'
import { useId, type ReactNode } from 'react'
import { useMediaQuery } from '../../hooks/useMediaQuery'
import { cn } from '../../lib/cn'
import ModalShell from './ModalShell'

interface DialogProps {
  open: boolean
  onClose: () => void
  title: string
  description?: string
  icon?: ReactNode
  tone?: 'brand' | 'danger'
  size?: 'md' | 'lg'
  children: ReactNode
  className?: string
}

// Felugró ablak: asztalin középen üvegpanel, mobilon lehúzással is bezárható alsó lap
export default function Dialog(props: DialogProps) {
  return <AnimatePresence>{props.open && <DialogPanel {...props} />}</AnimatePresence>
}

function DialogPanel({ onClose, title, description, icon, tone = 'brand', size = 'md', children, className }: DialogProps) {
  const titleId = useId()
  const descriptionId = useId()
  const desktop = useMediaQuery('(min-width: 640px)')
  const dragControls = useDragControls()

  const motionProps = desktop
    ? {
        initial: { opacity: 0, scale: 0.94, y: 24 },
        animate: { opacity: 1, scale: 1, y: 0 },
        exit: { opacity: 0, scale: 0.97, y: 12 },
        transition: { type: 'spring', stiffness: 380, damping: 30 } as const,
      }
    : {
        initial: { y: '100%' },
        animate: { y: 0 },
        exit: { y: '100%' },
        transition: { type: 'spring', stiffness: 320, damping: 34 } as const,
      }

  return (
    <ModalShell onClose={onClose} labelledBy={titleId} describedBy={description ? descriptionId : undefined}>
      <motion.section
        {...motionProps}
        drag={desktop ? false : 'y'}
        dragListener={false}
        dragControls={dragControls}
        dragConstraints={{ top: 0, bottom: 0 }}
        dragElastic={{ top: 0, bottom: 0.7 }}
        onDragEnd={(_, info) => {
          // Lefelé húzva (elég messzire vagy elég gyorsan) bezárul, egyébként visszaugrik
          if (info.offset.y > 110 || info.velocity.y > 600) {
            onClose()
          }
        }}
        className={cn(
          'pointer-events-auto relative flex max-h-[92dvh] w-full flex-col overflow-hidden rounded-t-[2rem] border border-b-0 border-white/60 bg-card/95 backdrop-blur-2xl',
          'shadow-[0_-24px_60px_-24px_rgb(24_10_50/0.45)] sm:max-h-[85dvh] sm:rounded-[2rem] sm:border-b sm:shadow-[0_40px_90px_-30px_rgb(24_10_50/0.6)] dark:border-white/10',
          size === 'lg' ? 'sm:max-w-2xl' : 'sm:max-w-lg',
          className,
        )}
      >
        <div
          aria-hidden
          className="pointer-events-none absolute inset-x-0 top-0 h-36 bg-[radial-gradient(90%_100%_at_0%_0%,rgb(168_120_245/0.22),transparent_70%)] dark:bg-[radial-gradient(90%_100%_at_0%_0%,rgb(124_69_209/0.3),transparent_70%)]"
        />

        <header
          onPointerDown={(event) => {
            if (!desktop) {
              dragControls.start(event)
            }
          }}
          className="relative touch-none px-6 pt-3 pb-4 sm:touch-auto sm:pt-6"
        >
          <div aria-hidden className="mx-auto mb-4 h-1.5 w-12 rounded-full bg-muted-foreground/30 sm:hidden" />
          <div className="flex items-start gap-4">
            {icon && (
              <span
                className={cn(
                  'flex size-12 shrink-0 items-center justify-center rounded-2xl text-white [&_svg]:size-6',
                  tone === 'danger' ? 'bg-danger-solid shadow-[0_14px_30px_-12px_rgb(225_29_72/0.6)]' : 'bg-brand-gradient shadow-glow',
                )}
              >
                {icon}
              </span>
            )}
            <div className="min-w-0 flex-1 pt-0.5">
              <h2 id={titleId} className="text-xl font-extrabold tracking-tight text-foreground">
                {title}
              </h2>
              {description && (
                <p id={descriptionId} className="mt-1 text-sm text-muted-foreground">
                  {description}
                </p>
              )}
            </div>
            <button
              type="button"
              aria-label="Bezárás"
              onClick={onClose}
              className="-mt-1 -mr-2 flex size-10 shrink-0 items-center justify-center rounded-full bg-muted/70 text-muted-foreground transition hover:rotate-90 hover:bg-muted hover:text-foreground focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
            >
              <X className="size-5" />
            </button>
          </div>
        </header>

        {/* Az alsó térköz belső elemen van, hogy a ragadós gombsor pontosan az ablak aljához tapadjon */}
        <div className="relative min-h-0 flex-1 overflow-y-auto overscroll-contain px-6">
          <div className="pb-[calc(1.5rem+env(safe-area-inset-bottom))] sm:pb-6">{children}</div>
        </div>
      </motion.section>
    </ModalShell>
  )
}

// Az ablak gombsora: görgetéskor is az ablak alján marad, mobilon a gombok egymás alatt
export function DialogActions({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <div
      className={cn(
        'sticky bottom-0 z-10 -mx-6 mt-6 -mb-[calc(1.5rem+env(safe-area-inset-bottom))] flex flex-col-reverse gap-3 border-t border-border bg-card/90 px-6 pt-4 pb-[calc(1rem+env(safe-area-inset-bottom))] backdrop-blur-xl',
        'sm:-mb-6 sm:flex-row sm:justify-end sm:pb-4',
        className,
      )}
    >
      {children}
    </div>
  )
}
