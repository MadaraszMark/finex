import { useEffect } from 'react'

// Egérkövető fényfolt a .spotlight kártyákon: egyetlen dokumentumszintű figyelő, renderelés nélkül
export function useSpotlight() {
  useEffect(() => {
    function handlePointerMove(event: PointerEvent) {
      if (event.pointerType !== 'mouse') {
        return
      }
      const target = event.target instanceof Element ? event.target.closest<HTMLElement>('.spotlight') : null
      if (!target) {
        return
      }
      const rect = target.getBoundingClientRect()
      target.style.setProperty('--spot-x', `${event.clientX - rect.left}px`)
      target.style.setProperty('--spot-y', `${event.clientY - rect.top}px`)
    }

    document.addEventListener('pointermove', handlePointerMove, { passive: true })
    return () => document.removeEventListener('pointermove', handlePointerMove)
  }, [])
}
