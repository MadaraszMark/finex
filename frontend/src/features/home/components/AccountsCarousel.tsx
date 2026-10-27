import { motion } from 'motion/react'
import type { AccountResponse } from '../../../api/types'
import { useBankSettings } from '../../../hooks/useBankSettings'
import { staggerContainer, staggerItem } from '../../../lib/motion'
import AccountCard from '../../accounts/components/AccountCard'
import NewAccountTile from '../../accounts/components/NewAccountTile'

interface AccountsCarouselProps {
  accounts: AccountResponse[]
  onOpenAccount: () => void
}

// Számlakártyák: mobilon vízszintesen lapozható sor, asztali nézetben rács
export default function AccountsCarousel({ accounts, onOpenAccount }: AccountsCarouselProps) {
  const { data: settings } = useBankSettings()
  const itemClass = 'w-[min(20rem,82vw)] shrink-0 snap-start lg:w-auto'

  return (
    <motion.div
      variants={staggerContainer}
      initial="hidden"
      animate="show"
      className="-mx-4 flex snap-x snap-mandatory scroll-px-4 gap-4 overflow-x-auto px-4 pt-1 pb-6 [scrollbar-width:none] sm:-mx-6 sm:scroll-px-6 sm:px-6 lg:mx-0 lg:grid lg:grid-cols-3 lg:gap-6 lg:overflow-visible lg:px-0"
    >
      {accounts.map((account) => (
        <motion.div key={account.id} variants={staggerItem} className={itemClass}>
          <AccountCard account={account} className="h-full" />
        </motion.div>
      ))}
      <motion.div variants={staggerItem} className={itemClass}>
        <NewAccountTile className="size-full min-h-48 w-full" disabled={settings !== undefined && accounts.length >= settings.maxAccountsPerUser} onClick={onOpenAccount} />
      </motion.div>
    </motion.div>
  )
}
