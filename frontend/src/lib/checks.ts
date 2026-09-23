import type { UseQueryResult } from '@tanstack/react-query'
import { getErrorMessage } from '../api/client'

// A rendszerteszt panel egy sora
export type CheckStatus = 'ok' | 'error' | 'pending' | 'idle'

export interface Check {
  label: string
  status: CheckStatus
  detail?: string
}

// Egy backend-lekérdezés állapotából ellenőrzési sort készít
export function queryCheck<T>(label: string, query: UseQueryResult<T>, describe: (data: T) => string): Check {
  if (query.isPending) {
    return { label, status: 'pending', detail: 'Ellenőrzés…' }
  }
  if (query.isError) {
    return { label, status: 'error', detail: getErrorMessage(query.error) }
  }
  return { label, status: 'ok', detail: describe(query.data) }
}
