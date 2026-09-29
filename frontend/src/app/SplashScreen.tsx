import AuroraBackground from '../components/brand/AuroraBackground'
import { LogoMark } from '../components/brand/Logo'

// Az alkalmazás indulásakor, amíg az első oldal kódja letöltődik (a belépő oldalak lila hátterén)
export default function SplashScreen() {
  return (
    <div role="status" aria-label="Betöltés" className="relative isolate flex min-h-dvh flex-col items-center justify-center gap-4 overflow-hidden text-white">
      <AuroraBackground />
      <LogoMark tone="light" size="lg" className="motion-safe:animate-pulse" />
      <p className="text-sm font-bold tracking-[0.3em] text-white/80 uppercase">FineX</p>
    </div>
  )
}
