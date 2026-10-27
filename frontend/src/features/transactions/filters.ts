import { useSearchParams } from 'react-router'
import type { TransactionSearchParams, TransactionType } from '../../api/types'

// A szűrés a címsorban tárolódik: megosztható, frissítés után megmarad, a Vissza gomb is működik

export type FilterParam = 'q' | 'account' | 'type' | 'category' | 'from' | 'to' | 'min' | 'max'

const TYPES: TransactionType[] = ['INCOME', 'OUTCOME', 'TRANSFER_IN', 'TRANSFER_OUT']
const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/

function positiveInt(value: string | null): number | undefined {
  const number = Number(value)
  return value && Number.isSafeInteger(number) && number > 0 ? number : undefined
}

function nonNegative(value: string | null): number | undefined {
  const number = Number(value)
  return value && Number.isFinite(number) && number >= 0 ? number : undefined
}

// A címsor paramétereiből a backend szűrője (az érvénytelen értékek kimaradnak)
export function parseFilter(params: URLSearchParams): TransactionSearchParams {
  const type = params.get('type')
  const from = params.get('from')
  const to = params.get('to')
  return {
    search: params.get('q')?.trim() || undefined,
    accountId: positiveInt(params.get('account')),
    type: TYPES.includes(type as TransactionType) ? (type as TransactionType) : undefined,
    categoryId: positiveInt(params.get('category')),
    from: from && ISO_DATE.test(from) ? from : undefined,
    to: to && ISO_DATE.test(to) ? to : undefined,
    minAmount: nonNegative(params.get('min')),
    maxAmount: nonNegative(params.get('max')),
  }
}

export function useTransactionFilter() {
  const [params, setParams] = useSearchParams()
  const filter = parseFilter(params)
  const activeCount = Object.values(filter).filter((value) => value !== undefined).length

  // Gépelés közben helyben cseréljük a címet (replace), hogy ne teljen meg az előzmény
  function update(patch: Partial<Record<FilterParam, string | undefined>>, options: { replace?: boolean } = {}) {
    setParams(
      (current) => {
        const next = new URLSearchParams(current)
        for (const [key, value] of Object.entries(patch)) {
          if (value) {
            next.set(key, value)
          } else {
            next.delete(key)
          }
        }
        // A megnyitott tétel (tx) szűréskor bezárul
        next.delete('tx')
        return next
      },
      { replace: options.replace },
    )
  }

  function clear() {
    setParams(new URLSearchParams())
  }

  // A megnyitott tétel is a címsorban van (?tx=123), így linkelhető
  const selectedId = positiveInt(params.get('tx'))

  // Megnyitáskor új előzmény (a Vissza bezárja), bezáráskor csak cseréljük a címet
  function select(id: number | undefined) {
    setParams(
      (current) => {
        const next = new URLSearchParams(current)
        if (id === undefined) {
          next.delete('tx')
        } else {
          next.set('tx', String(id))
        }
        return next
      },
      { replace: id === undefined },
    )
  }

  return { filter, activeCount, update, clear, selectedId, select }
}
