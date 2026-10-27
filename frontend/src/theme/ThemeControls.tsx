import { Monitor, Moon, Sun } from 'lucide-react'
import IconButton from '../components/ui/IconButton'
import type { IconButtonTone } from '../components/ui/buttonStyles'
import SegmentedControl, { type SegmentedOption } from '../components/ui/SegmentedControl'
import type { ThemePreference } from './themeStore'
import { changeTheme, elementCenter } from './themeTransition'
import { useTheme } from './useTheme'

const themeOptions: SegmentedOption<ThemePreference>[] = [
  { value: 'light', label: 'Világos', icon: <Sun /> },
  { value: 'dark', label: 'Sötét', icon: <Moon /> },
  { value: 'system', label: 'Rendszer', icon: <Monitor /> },
]

// Háromállású témaválasztó; az új téma a megnyomott gombtól induló körben terül szét
export function ThemeSwitcher({ className }: { className?: string }) {
  const { preference } = useTheme()

  return (
    <SegmentedControl
      label="Megjelenés"
      value={preference}
      options={themeOptions}
      onChange={(value) => changeTheme(value, elementCenter(document.activeElement))}
      className={className}
    />
  )
}

// Gyors váltógomb világos és sötét mód között (a belépő oldalak sarkában)
export function ThemeToggleButton({ tone }: { tone?: IconButtonTone }) {
  const { resolved } = useTheme()
  const isDark = resolved === 'dark'

  return (
    <IconButton
      label={isDark ? 'Váltás világos módra' : 'Váltás sötét módra'}
      tone={tone}
      onClick={(event) => changeTheme(isDark ? 'light' : 'dark', elementCenter(event.currentTarget))}
    >
      {isDark ? <Sun /> : <Moon />}
    </IconButton>
  )
}
