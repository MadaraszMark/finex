import { AnimatePresence, motion } from 'motion/react'

// Vékony lila csík a képernyő tetején, amíg a következő oldal kódja betöltődik (lazy route)
export default function NavigationProgress({ active }: { active: boolean }) {
  return (
    <AnimatePresence>
      {active && (
        <motion.div
          role="progressbar"
          aria-label="Oldal betöltése"
          className="fixed inset-x-0 top-0 z-[60] h-1 origin-left bg-brand-gradient"
          initial={{ scaleX: 0 }}
          animate={{ scaleX: 0.85, transition: { duration: 2.5, ease: 'easeOut' } }}
          exit={{ scaleX: 1, opacity: 0, transition: { duration: 0.3 } }}
        />
      )}
    </AnimatePresence>
  )
}
