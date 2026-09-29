import { QueryClientProvider } from '@tanstack/react-query'
import { MotionConfig } from 'motion/react'
import { RouterProvider } from 'react-router/dom'
import { Toaster } from 'sonner'
import { queryClient } from '../lib/queryClient'
import SessionWatcher from '../session/SessionWatcher'
import { useTheme } from '../theme/useTheme'
import { router } from './router'

// Az értesítő buborékok a világos/sötét témát követik (külön komponens, hogy témaváltáskor csak ez rajzolódjon újra)
function ThemedToaster() {
  const { resolved } = useTheme()
  return <Toaster theme={resolved} position="top-center" richColors closeButton />
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
