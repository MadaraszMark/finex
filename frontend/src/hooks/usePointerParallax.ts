import { useMotionValue, useReducedMotion, useSpring, type MotionValue } from 'motion/react'
import { useEffect } from 'react'

export interface PointerParallax {
  // Az egér helyzete a képernyő közepéhez képest, -0,5 … 0,5 között, rugós késleltetéssel
  x: MotionValue<number>
  y: MotionValue<number>
}

// Az egér mozgását követő értékek a "mélységhatáshoz" (díszítőelemek, döntött bankkártya).
// React-renderelés nélkül frissülnek, így nem lassítják az oldalt; csökkentett mozgásnál kikapcsol.
export function usePointerParallax(): PointerParallax {
  const reduceMotion = useReducedMotion()
  const rawX = useMotionValue(0)
  const rawY = useMotionValue(0)
  const x = useSpring(rawX, { stiffness: 60, damping: 20 })
  const y = useSpring(rawY, { stiffness: 60, damping: 20 })

  useEffect(() => {
    if (reduceMotion) {
      return
    }

    function handlePointerMove(event: PointerEvent) {
      rawX.set(event.clientX / window.innerWidth - 0.5)
      rawY.set(event.clientY / window.innerHeight - 0.5)
    }

    window.addEventListener('pointermove', handlePointerMove)
    return () => window.removeEventListener('pointermove', handlePointerMove)
  }, [reduceMotion, rawX, rawY])

  return { x, y }
}
