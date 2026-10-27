export interface ChartTooltipRow {
  key: string
  label: string
  value: string
  // A sorozat színe (CSS-változó is lehet); a sor elején rövid vonal jelöli
  color: string
}

// A grafikonok közös buboréka: hangsúlyos érték, mellette halványabb sorozatnév
export default function ChartTooltip({ title, rows }: { title: string; rows: ChartTooltipRow[] }) {
  return (
    <div className="min-w-44 rounded-2xl border border-border bg-card/95 px-3.5 py-3 text-sm shadow-xl backdrop-blur-xl">
      <p className="text-xs font-semibold text-muted-foreground">{title}</p>
      <ul className="mt-2 space-y-1.5">
        {rows.map((row) => (
          <li key={row.key} className="flex items-center gap-2.5">
            <span aria-hidden className="h-0.5 w-3 shrink-0 rounded-full" style={{ backgroundColor: row.color }} />
            <span className="font-bold text-foreground tabular-nums">{row.value}</span>
            <span className="text-muted-foreground">{row.label}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}
