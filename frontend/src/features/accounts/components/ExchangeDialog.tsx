import { ArrowDown, ArrowRightLeft } from 'lucide-react'
import { useRef, useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import AmountField from '../../../components/ui/AmountField'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import Select from '../../../components/ui/Select'
import { useBankSettings } from '../../../hooks/useBankSettings'
import { formatMoney, formatShortIban, parseAmount } from '../../../lib/format'
import { newIdempotencyKey } from '../../../lib/idempotency'
import { useExchange } from '../queries'

const rateFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 6 })

interface ExchangeDialogProps {
  accounts: AccountResponse[]
  open: boolean
  onClose: () => void
}

// Előnézet a backend képletével (forinton át, lefelé kerekítve); a végösszeg a szerveré
function previewAmount(amount: number, fromRate: number, toRate: number): number {
  return Math.floor(((amount * fromRate) / toRate) * 100) / 100
}

// Devizaváltás két saját számla között a bank árfolyamán (GET /settings)
export default function ExchangeDialog({ accounts, open, onClose }: ExchangeDialogProps) {
  const exchange = useExchange()
  const { data: settings } = useBankSettings()
  const active = accounts.filter((account) => account.status === 'ACTIVE')
  const [fromId, setFromId] = useState('')
  const [toId, setToId] = useState('')
  const [amountInput, setAmountInput] = useState('')
  const [error, setError] = useState<string>()
  // Ablakmegnyitásonként egy kulcs: újrapróbáláskor a szerver nem könyvel kétszer
  const idempotencyKey = useRef(newIdempotencyKey())

  const from = active.find((account) => String(account.id) === fromId) ?? active[0]
  const targets = active.filter((account) => from && account.currency !== from.currency)
  const to = targets.find((account) => String(account.id) === toId) ?? targets[0]
  const rates = settings?.exchangeRates
  const fromRate = from && rates?.[from.currency]
  const toRate = to && rates?.[to.currency]
  const amount = parseAmount(amountInput)
  const converted = amount !== undefined && fromRate && toRate ? previewAmount(amount, fromRate, toRate) : undefined

  function close() {
    onClose()
    setFromId('')
    setToId('')
    setAmountInput('')
    setError(undefined)
    exchange.reset()
    idempotencyKey.current = newIdempotencyKey()
  }

  function submit() {
    if (!from || !to) {
      return
    }
    if (amount === undefined || amount <= 0) {
      setError('Adj meg egy nullánál nagyobb összeget.')
      return
    }
    if (from.currency === 'HUF' && !Number.isInteger(amount)) {
      setError('Egész forintösszeget adj meg.')
      return
    }
    if (amount > from.balance) {
      setError(`Nincs elég fedezet: a számla egyenlege ${formatMoney(from.balance, from.currency)}.`)
      return
    }
    setError(undefined)

    exchange.mutate(
      { request: { fromAccountId: from.id, toAccountId: to.id, amount }, idempotencyKey: idempotencyKey.current },
      {
        onSuccess: (result) => {
          close()
          toast.success('Sikeres váltás', {
            description: `${formatMoney(result.fromAmount, result.fromCurrency)} → ${formatMoney(result.toAmount, result.toCurrency)}`,
          })
        },
      },
    )
  }

  return (
    <Dialog icon={<ArrowRightLeft />} open={open} onClose={close} title="Devizaváltás" description="Pénz átváltása két saját számlád között a bank árfolyamán.">
      {targets.length === 0 || !from ? (
        <Alert tone="info">A váltáshoz két különböző pénznemű aktív számla kell. Nyiss egy devizaszámlát a Számlák oldalon.</Alert>
      ) : (
        <form
          noValidate
          onSubmit={(event) => {
            event.preventDefault()
            submit()
          }}
          className="space-y-5"
        >
          <Select
            label="Erről a számláról"
            value={String(from.id)}
            onChange={(event) => {
              setFromId(event.target.value)
              setToId('')
            }}
          >
            {active.map((account) => (
              <option key={account.id} value={account.id}>
                {account.name} · {formatShortIban(account.accountNumber)} · {formatMoney(account.balance, account.currency)}
              </option>
            ))}
          </Select>

          <AmountField label="Összeg" currency={from.currency} placeholder={from.currency === 'HUF' ? '100 000' : '100'} value={amountInput} onChange={(event) => setAmountInput(event.target.value)} error={error} />

          <div className="flex justify-center text-muted-foreground">
            <ArrowDown aria-hidden className="size-5" />
          </div>

          <Select label="Erre a számlára" value={to ? String(to.id) : ''} onChange={(event) => setToId(event.target.value)}>
            {targets.map((account) => (
              <option key={account.id} value={account.id}>
                {account.name} · {formatShortIban(account.accountNumber)} · {formatMoney(account.balance, account.currency)}
              </option>
            ))}
          </Select>

          {to && fromRate && toRate && (
            <div className="rounded-2xl bg-muted/60 p-4 text-sm">
              <p className="flex items-baseline justify-between gap-3">
                <span className="text-muted-foreground">Jóváírás (előnézet)</span>
                <span className="text-lg font-bold text-foreground tabular-nums">{converted !== undefined ? formatMoney(converted, to.currency) : '–'}</span>
              </p>
              <p className="mt-1 text-muted-foreground">
                Árfolyam: 1 {from.currency} = {rateFormatter.format(fromRate / toRate)} {to.currency}
              </p>
            </div>
          )}

          {exchange.isError && <Alert tone="danger">{getErrorMessage(exchange.error)}</Alert>}

          <DialogActions>
            <Button variant="ghost" onClick={close}>
              Mégse
            </Button>
            <Button type="submit" leftIcon={<ArrowRightLeft />} loading={exchange.isPending}>
              Váltás
            </Button>
          </DialogActions>
        </form>
      )}
    </Dialog>
  )
}
