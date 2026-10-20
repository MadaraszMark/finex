import { Plus } from 'lucide-react'
import { cn } from '../../../lib/cn'

// Szaggatott keretes "üres kártya" a számlák sorának végén: új számla nyitása
export default function NewAccountTile({ onClick, disabled, className }: { onClick: () => void; disabled?: boolean; className?: string }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      title={disabled ? 'Elérted a nyitott számlák legnagyobb számát.' : undefined}
      className={cn(
        'spotlight group flex flex-col items-center justify-center gap-3 rounded-[1.75rem] border-2 border-dashed border-brand-300/80 bg-card/60 p-6 text-center backdrop-blur-xl transition duration-300',
        'hover:border-brand-400 hover:bg-card/80 focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none',
        'disabled:cursor-not-allowed disabled:opacity-60 dark:border-brand-700/70',
        className,
      )}
    >
      <span className="flex size-12 items-center justify-center rounded-2xl bg-brand-gradient text-white shadow-glow transition-transform duration-300 group-hover:scale-110 group-disabled:scale-100">
        <Plus className="size-6" />
      </span>
      <span>
        <span className="block font-bold text-foreground">Új számla</span>
        <span className="block text-sm text-muted-foreground">Forint, euró vagy dollár</span>
      </span>
    </button>
  )
}
