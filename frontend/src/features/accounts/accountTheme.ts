import type { AccountResponse } from '../../api/types'

interface AccountTheme {
  surface: string
  glow: string
}

// Pénznemenként saját színvilág, mint a koncepcióképen (a forintszámla lila, a devizaszámla türkiz kártya).
// A színátmenetek elég sötétek ahhoz, hogy a fehér felirat mindenhol jól olvasható legyen.
const themes: Record<string, AccountTheme> = {
  HUF: {
    surface: 'bg-[linear-gradient(135deg,#a878f5_0%,#8a4fe0_45%,#5f33b5_100%)]',
    glow: 'shadow-[0_24px_48px_-24px_rgb(95_51_181/0.75)]',
  },
  EUR: {
    surface: 'bg-[linear-gradient(135deg,#2aa9c5_0%,#137ea5_50%,#0b4f78_100%)]',
    glow: 'shadow-[0_24px_48px_-24px_rgb(11_79_120/0.7)]',
  },
  USD: {
    surface: 'bg-[linear-gradient(135deg,#25a882_0%,#118263_50%,#0a5a45_100%)]',
    glow: 'shadow-[0_24px_48px_-24px_rgb(10_90_69/0.7)]',
  },
}

const closedTheme: AccountTheme = {
  surface: 'bg-[linear-gradient(135deg,#a1a1aa_0%,#71717a_50%,#52525b_100%)]',
  glow: 'shadow-[0_24px_48px_-28px_rgb(39_39_42/0.6)]',
}

export function accountTheme(account: Pick<AccountResponse, 'currency' | 'status'>): AccountTheme {
  if (account.status === 'CLOSED') {
    return closedTheme
  }
  return themes[account.currency] ?? themes.HUF
}
