import { Check, Monitor, Moon, Sun, type LucideIcon } from 'lucide-react'
import { Card, CardHeader } from '../../../components/ui/Card'
import { cn } from '../../../lib/cn'
import type { ThemePreference } from '../../../theme/themeStore'
import { changeTheme, elementCenter } from '../../../theme/themeTransition'
import { useTheme } from '../../../theme/useTheme'

const options: { value: ThemePreference; label: string; icon: LucideIcon; description: string }[] = [
  { value: 'light', label: 'Világos', icon: Sun, description: 'Nappali fényhez' },
  { value: 'dark', label: 'Sötét', icon: Moon, description: 'Esti használathoz' },
  { value: 'system', label: 'Rendszer', icon: Monitor, description: 'Az eszközöd beállítása' },
]

// Kis "képernyőkép" a témáról: világos vagy sötét felület a lila fejlécsávval (a rendszer témánál kettéosztva).
// A sávok magassága százalékos, így kicsiben (lista) és nagyban (kártya) is arányos.
function ThemePreview({ theme, className }: { theme: ThemePreference; className?: string }) {
  const panel = (dark: boolean) => (
    <div className={cn('flex flex-1 flex-col gap-[7%] p-[8%]', dark ? 'bg-[#0d0a16]' : 'bg-[#f7f5fc]')}>
      <div className={cn('h-[20%] rounded-md', dark ? 'bg-[linear-gradient(135deg,#231440,#150c2a)]' : 'bg-[linear-gradient(135deg,#a878f5,#5f33b5)]')} />
      <div className={cn('h-[32%] rounded-md', dark ? 'bg-[#16121f]' : 'bg-white')} />
      <div className={cn('h-[10%] w-2/3 rounded', dark ? 'bg-[#2c2542]' : 'bg-[#e8e2f4]')} />
    </div>
  )

  return (
    <div aria-hidden className={cn('flex overflow-hidden rounded-xl border border-border', className)}>
      {theme === 'system' ? (
        <>
          {panel(false)}
          {panel(true)}
        </>
      ) : (
        panel(theme === 'dark')
      )}
    </div>
  )
}

// Megjelenés: a három téma előnézettel; választáskor a kattintás helyéről körben terül szét az új téma.
// A kártya konténer: keskeny oszlopban a témák egymás alatt, kis előnézettel; elég helyen három nagy kártya egymás mellett.
export default function AppearanceCard() {
  const { preference } = useTheme()

  return (
    <Card className="@container flex flex-col p-6 sm:p-7">
      <CardHeader title="Megjelenés" description="Válaszd ki, hogyan jelenjen meg a FineX." />
      <div role="radiogroup" aria-label="Téma" className="mt-5 grid flex-1 auto-rows-fr grid-cols-1 gap-3 @md:grid-cols-3">
        {options.map((option) => {
          const selected = preference === option.value
          return (
            <button
              key={option.value}
              type="button"
              role="radio"
              aria-checked={selected}
              onClick={(event) => changeTheme(option.value, elementCenter(event.currentTarget))}
              className={cn(
                'flex items-center gap-3.5 rounded-2xl border p-3 text-left transition duration-200 focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none @md:block',
                selected ? 'border-brand-400 bg-brand-50 shadow-glow dark:border-brand-500/60 dark:bg-brand-950/60' : 'border-border hover:border-brand-300',
              )}
            >
              <ThemePreview theme={option.value} className="h-14 w-20 shrink-0 @md:h-24 @md:w-auto" />
              <span className="block min-w-0 flex-1 @md:mt-3">
                <span className="flex items-center gap-2">
                  <option.icon aria-hidden className="size-4 shrink-0 text-primary" />
                  <span className="min-w-0 flex-1 truncate font-semibold text-foreground">{option.label}</span>
                  {selected && <Check aria-hidden className="size-4 shrink-0 text-primary" />}
                </span>
                <span className="mt-0.5 block text-xs text-muted-foreground">{option.description}</span>
              </span>
            </button>
          )
        })}
      </div>
    </Card>
  )
}
