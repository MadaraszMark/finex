import { motion } from 'motion/react'
import { useEffect, useRef, useState, type ReactNode } from 'react'

// A tartalom magasságának változását (pl. belépés ↔ regisztráció) simán, rugószerűen animálja.
// A belső elem valódi magasságát ResizeObserver méri; a -m-2/p-2 pár miatt a mezők fókuszkerete nem vágódik le.
export default function AnimatedHeight({ children }: { children: ReactNode }) {
  const innerRef = useRef<HTMLDivElement>(null)
  const [height, setHeight] = useState<number | 'auto'>('auto')

  useEffect(() => {
    const element = innerRef.current
    if (!element) {
      return
    }

    const observer = new ResizeObserver(() => setHeight(element.offsetHeight))
    observer.observe(element)
    return () => observer.disconnect()
  }, [])

  return (
    <motion.div animate={{ height }} transition={{ type: 'spring', bounce: 0.15, duration: 0.5 }} className="-m-2 overflow-hidden">
      <div ref={innerRef} className="p-2">
        {children}
      </div>
    </motion.div>
  )
}
