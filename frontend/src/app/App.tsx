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

// Saját popover-rétegben, hogy a buborék a nyitott ablak fölött is látsszon (lib/topLayer)
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

// Az alkalmazás gyökere: adatlekérés, animációk, útvonalak és értesítő buborékok
export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <MotionConfig reducedMotion="user">
        <SessionWatcher />
        <RouterProvider router={router} />
        <ThemedToaster />
      </MotionConfig>
    </QueryClientProvider>
  )
}
