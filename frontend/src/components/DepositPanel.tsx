import { LoaderCircle, Plus } from 'lucide-react'
import { formatMoney } from '../lib/format'

const TEST_AMOUNTS = [1000, 10000, 50000]

interface DepositPanelProps {
  currency: string
  isPending: boolean
  errorMessage?: string
  onDeposit: (amount: number) => void
}

// Adatbázis-írás próbája: a gombok a POST /accounts/{id}/deposit végpontot hívják
export default function DepositPanel({ currency, isPending, errorMessage, onDeposit }: DepositPanelProps) {
  return (
    <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
      <div className="flex items-center justify-between">
        <h2 className="font-semibold text-slate-900">Teszt befizetés</h2>
        {isPending && <LoaderCircle className="size-4 animate-spin text-brand-500" />}
      </div>
      <p className="mt-1 text-sm text-slate-500">
        Az adatbázis-írást próbálja ki: a{' '}
        <code className="rounded bg-slate-100 px-1.5 py-0.5 text-xs text-slate-700">POST /accounts/{'{id}'}/deposit</code>{' '}
        végpontot hívja.
      </p>

      <div className="mt-4 flex flex-wrap gap-2">
        {TEST_AMOUNTS.map((amount) => (
          <button
            key={amount}
            type="button"
            disabled={isPending}
            onClick={() => onDeposit(amount)}
            className="inline-flex items-center gap-1.5 rounded-xl bg-brand-50 px-4 py-2 text-sm font-semibold text-brand-700 ring-1 ring-brand-200 transition hover:bg-brand-100 disabled:cursor-not-allowed disabled:opacity-60"
          >
            <Plus className="size-4" />
            {formatMoney(amount, currency)}
          </button>
        ))}
      </div>

      {errorMessage && <p className="mt-3 text-sm text-red-600">{errorMessage}</p>}
    </section>
  )
}
