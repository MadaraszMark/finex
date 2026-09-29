import { RefreshCw, TriangleAlert } from 'lucide-react'
import { isRouteErrorResponse, useRouteError } from 'react-router'
import AuroraBackground from '../components/brand/AuroraBackground'
import Button from '../components/ui/Button'
import { buttonStyles } from '../components/ui/buttonStyles'

// Váratlan hiba egy oldal megjelenítése közben (pl. egy új verzió kitelepítése után már nem létező kódrészlet).
// Az útvonalak "errorElement"-je, így egy hiba nem hagyja üresen a képernyőt.
export default function RouteErrorPage() {
  const error = useRouteError()
  const detail = isRouteErrorResponse(error) ? `${error.status} ${error.statusText}` : error instanceof Error ? error.message : null

  return (
    <div className="relative isolate flex min-h-dvh items-center justify-center overflow-hidden px-4 py-10">
      <AuroraBackground />

      <div className="w-full max-w-md rounded-[2rem] border border-white/60 bg-card/90 p-8 text-center shadow-2xl backdrop-blur-2xl motion-safe:animate-fade-up sm:p-10 dark:border-white/10 dark:bg-card/85">
        <span className="mx-auto flex size-16 items-center justify-center rounded-3xl bg-danger/10 text-danger">
          <TriangleAlert className="size-8" />
        </span>
        <h1 className="mt-6 text-2xl font-extrabold tracking-tight">Hoppá, valami hiba történt</h1>
        <p className="mt-2 text-muted-foreground">Az oldalt nem sikerült megjeleníteni. Töltsd újra, és ha a hiba megmarad, próbáld később.</p>
        {import.meta.env.DEV && detail && (
          <pre className="mt-4 overflow-x-auto rounded-2xl bg-muted p-3 text-left text-xs text-muted-foreground">{detail}</pre>
        )}
        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Button leftIcon={<RefreshCw />} onClick={() => window.location.reload()}>
            Oldal újratöltése
          </Button>
          {/* Sima link: a teljes alkalmazás újraindul, nem csak ez az oldal */}
          <a href="/" className={buttonStyles({ variant: 'outline' })}>
            Vissza a főoldalra
          </a>
        </div>
      </div>
    </div>
  )
}
