import Spinner from './Spinner'

// Egy oldal (vagy az oldal tartalmi része) betöltése közben
export default function PageLoader({ label = 'Betöltés…' }: { label?: string }) {
  return (
    <div className="flex min-h-[40vh] flex-col items-center justify-center gap-3 text-muted-foreground">
      <Spinner className="size-7 text-primary" label={label} />
      <p className="text-sm">{label}</p>
    </div>
  )
}
