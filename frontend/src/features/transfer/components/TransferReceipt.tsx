import { Check, House, ReceiptText, Send } from 'lucide-react'
import { motion } from 'motion/react'
import type { TransferResponse } from '../../../api/types'
import Badge from '../../../components/ui/Badge'
import Button from '../../../components/ui/Button'
import ButtonLink from '../../../components/ui/ButtonLink'
import { Card } from '../../../components/ui/Card'
import { formatDateTime, formatIban, formatMoney, formatSignedMoney } from '../../../lib/format'
import { staggerContainer, staggerItem } from '../../../lib/motion'

// 5. lépés: a nyugta az utalás adataival
export default function TransferReceipt({ receipt, onNewTransfer }: { receipt: TransferResponse; onNewTransfer: () => void }) {
  const rows = [
    { label: 'Kinek', value: receipt.partnerName },
    { label: 'Számlaszám', value: formatIban(receipt.toAccountNumber), mono: true },
    { label: 'Közlemény', value: receipt.message ?? undefined },
    { label: 'Kategória', value: receipt.categories.map((category) => category.name).join(', ') || undefined },
    { label: 'Új egyenleg', value: formatMoney(receipt.fromAccountNewBalance, receipt.currency) },
    { label: 'Időpont', value: formatDateTime(receipt.createdAt) },
    { label: 'Azonosító', value: `#${receipt.transactionId}`, mono: true },
  ].filter((row) => row.value)

  return (
    <Card className="mx-auto max-w-xl p-6 text-center sm:p-10">
      <motion.div variants={staggerContainer} initial="hidden" animate="show">
        <motion.span
          initial={{ scale: 0.3, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ type: 'spring', stiffness: 260, damping: 14 }}
          className="relative mx-auto flex size-20 items-center justify-center rounded-full bg-emerald-600 text-white shadow-[0_18px_40px_-16px_rgb(5_150_105/0.8)]"
        >
          <span aria-hidden className="absolute inset-0 rounded-full ring-2 ring-emerald-400/70 motion-safe:animate-pulse-ring" />
          <Check className="size-10" strokeWidth={3} />
        </motion.span>

        <motion.h2 variants={staggerItem} className="mt-6 text-2xl font-extrabold tracking-tight text-foreground">
          Sikeres utalás
        </motion.h2>
        <motion.p variants={staggerItem} className="mt-2 text-4xl font-extrabold tracking-tight break-words text-foreground">
          {formatSignedMoney(-receipt.amount, receipt.currency)}
        </motion.p>
        <motion.div variants={staggerItem} className="mt-3 flex justify-center">
          <Badge tone={receipt.internal ? 'success' : 'brand'}>{receipt.internal ? 'Azonnal megérkezett' : 'Másik bank számlájára elküldve'}</Badge>
        </motion.div>

        <motion.dl variants={staggerItem} className="mt-6 divide-y divide-border rounded-2xl border border-border text-left">
          {rows.map((row) => (
            <div key={row.label} className="flex flex-col gap-0.5 px-4 py-3 sm:flex-row sm:items-baseline sm:justify-between sm:gap-4">
              <dt className="text-sm text-muted-foreground">{row.label}</dt>
              <dd className={`text-sm font-semibold break-all text-foreground sm:text-right ${row.mono ? 'font-mono tracking-wide' : ''}`}>{row.value}</dd>
            </div>
          ))}
        </motion.dl>

        <motion.div variants={staggerItem} className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Button leftIcon={<Send />} onClick={onNewTransfer}>
            Új utalás
          </Button>
          <ButtonLink to={`/transactions?tx=${receipt.transactionId}`} variant="outline" leftIcon={<ReceiptText />}>
            Tétel megtekintése
          </ButtonLink>
          <ButtonLink to="/" variant="ghost" leftIcon={<House />}>
            Főoldal
          </ButtonLink>
        </motion.div>
      </motion.div>
    </Card>
  )
}
