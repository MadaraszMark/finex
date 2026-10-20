import type { AccountStatus } from '../../api/types'
import type { BadgeTone } from '../../components/ui/Badge'

// A számlaállapotok jelvényszíne az admin felületen (táblázat, állapotváltó ablak, ügyfél adatlapja)
export const accountStatusTones: Record<AccountStatus, BadgeTone> = {
  ACTIVE: 'success',
  FROZEN: 'warning',
  BLOCKED: 'danger',
  CLOSED: 'neutral',
}
