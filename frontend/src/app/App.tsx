import { QueryClientProvider } from '@tanstack/react-query'
import { MotionConfig } from 'motion/react'
import { useEffect, useRef } from 'react'
import { RouterProvider } from 'react-router/dom'
import { Toaster } from 'sonner'
import { queryClient } from '../lib/queryClient'
import { onRaiseToasts } from '../lib/topLayer'
import SessionWatcher from '../session/SessionWatcher'
import { useTheme } from '../theme/useTheme'
import { router } from './router'

// Az értesítő buborékok a világos/sötét témát követik (külön komponens, hogy témaváltáskor csak ez rajzolódjon újra).
// Saját rétegük (popover) a böngésző legfelső rétegében van, és minden felugró ablak megnyitásakor újra a tetejére
// kerül (lib/topLayer): így a visszajelzés a nyitott ablak fölött is látszik, nem vész el a sötétített háttér mögött.
function ThemedToaster() {
  const { resolved } = useTheme()
  const layerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const layer = layerRef.current
    if (!layer || typeof layer.showPopover !== 'function') {
      return
    }
    const raise = () => {
      if (layer.matches(':popover-open')) {
        layer.hidePopover()
      }
      layer.showPopover()
    }
    raise()
    return onRaiseToasts(raise)
  }, [])

  return (
    <div ref={layerRef} popover="manual" className="m-0 size-0 overflow-visible border-0 bg-transparent p-0">
      <Toaster theme={resolved} position="top-center" richColors closeButton />
    </div>
  )
}

// Az alkalmazás gyökere: adatlekérés (React Query), animációk, útvonalak és értesítő buborékok
export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      {/* Aki az operációs rendszerben kikapcsolta az animációkat, annak mozgás nélkül jelenik meg minden */}
      <MotionConfig reducedMotion="user">
        <SessionWatcher />
        <RouterProvider router={router} />
        <ThemedToaster />
      </MotionConfig>
    </QueryClientProvider>
  )
}
