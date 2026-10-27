import { motion } from 'motion/react'
import { useRef, type KeyboardEvent } from 'react'
import type { AccountResponse, CardResponse } from '../../../api/types'
import FinexCard from '../../../components/brand/FinexCard'
import { cn } from '../../../lib/cn'
import { cardStatusLabels, formatCardExpiry, formatMaskedCard } from '../../../lib/format'
import { staggerContainer, staggerItem } from '../../../lib/motion'
import { accountTheme } from '../../accounts/accountTheme'

interface CardPickerProps {
  cards: CardResponse[]
  accounts: AccountResponse[]
  selectedId: number
  onSelect: (id: number) => void
}

// Kártyaválasztó rádiócsoportként (nyilakkal is váltható); mobilon vízszintesen lapozható
export default function CardPicker({ cards, accounts, selectedId, onSelect }: CardPickerProps) {
  const buttonsRef = useRef<Array<HTMLButtonElement | null>>([])
  const selectedIndex = cards.findIndex((card) => card.id === selectedId)

  function handleKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    const forward = event.key === 'ArrowRight' || event.key === 'ArrowDown'
    const backward = event.key === 'ArrowLeft' || event.key === 'ArrowUp'
    if (!forward && !backward) {
      return
    }
    event.preventDefault()
    const nextIndex = (selectedIndex + (forward ? 1 : -1) + cards.length) % cards.length
    onSelect(cards[nextIndex].id)
    buttonsRef.current[nextIndex]?.focus()
  }

  return (
    <motion.div
      role="radiogroup"
      aria-label="Bankkártyák"
      onKeyDown={handleKeyDown}
      variants={staggerContainer}
      initial="hidden"
      animate="show"
      className="-mx-4 flex snap-x snap-mandatory gap-4 overflow-x-auto px-4 pt-1 pb-6 [scrollbar-width:none] sm:-mx-6 sm:px-6 lg:mx-0 lg:flex-col lg:gap-5 lg:overflow-visible lg:px-0 lg:pt-0 lg:pb-0"
    >
      {cards.map((card, index) => {
        const account = accounts.find((item) => item.id === card.accountId)
        const selected = card.id === selectedId
        return (
          <motion.button
            key={card.id}
            ref={(element) => {
              buttonsRef.current[index] = element
            }}
            variants={staggerItem}
            type="button"
            role="radio"
            aria-checked={selected}
            aria-label={`${account?.name ?? 'Kártya'}, ${formatMaskedCard(card.maskedNumber)}, ${cardStatusLabels[card.status]}`}
            tabIndex={selected ? 0 : -1}
            onClick={() => onSelect(card.id)}
            className={cn(
              'group w-[min(20rem,82vw)] shrink-0 snap-center rounded-[2rem] p-1.5 text-left transition-[background-color,box-shadow] duration-300 lg:w-full',
              'focus-visible:ring-4 focus-visible:ring-ring/50 focus-visible:outline-none',
              selected && 'bg-brand-gradient shadow-glow',
            )}
          >
            {/* Halványítás belül: a gomb átlátszóságát a beúszó animáció állítja */}
            <span aria-hidden className={cn('block transition-opacity duration-300', !selected && 'opacity-70 group-hover:opacity-100')}>
              <span className="block h-52">
                <FinexCard
                  variant="solid"
                  holderName={card.holderName}
                  maskedNumber={formatMaskedCard(card.maskedNumber)}
                  expiry={formatCardExpiry(card.expiryDate)}
                  status={card.status}
                  className={account ? accountTheme({ currency: account.currency, status: 'ACTIVE' }).surface : undefined}
                />
              </span>
              <span className={cn('flex items-center justify-between gap-3 px-3 pt-2.5 pb-1.5 text-sm font-semibold', selected ? 'text-white' : 'text-foreground')}>
                <span className="truncate">{account?.name ?? 'Kártya'}</span>
                <span className={cn('shrink-0', selected ? 'text-white/80' : 'text-muted-foreground')}>{account?.currency}</span>
              </span>
            </span>
          </motion.button>
        )
      })}
    </motion.div>
  )
}
