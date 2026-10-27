import { api } from './client'
import type { CategorySpendingResponse, DashboardSummaryResponse, MonthlySummaryResponse, TransactionListItem } from './types'

// StatisticsController (/statistics): összesítések a bejelentkezett felhasználó számláiról

// Teljes egyenleg, megtakarítások és a folyó hónap bevétele/kiadása egy devizanemben
export async function getSummary(currency: string): Promise<DashboardSummaryResponse> {
  const { data } = await api.get<DashboardSummaryResponse>('/statistics/summary', { params: { currency } })
  return data
}

// Havi bevétel és kiadás, a legrégebbi elöl (a forgalom nélküli hónapok is benne vannak)
export async function getMonthlySummary(months: number, currency: string): Promise<MonthlySummaryResponse[]> {
  const { data } = await api.get<MonthlySummaryResponse[]>('/statistics/monthly', { params: { months, currency } })
  return data
}

// Kiadás kategóriánként, a legnagyobb elöl; a from és a to "ÉÉÉÉ-HH-NN", mindkét nap beleszámít
export async function getCategorySpending(from: string, to: string, currency: string): Promise<CategorySpendingResponse[]> {
  const { data } = await api.get<CategorySpendingResponse[]>('/statistics/categories', { params: { from, to, currency } })
  return data
}

// Az időszak legnagyobb kiadásai a pénznemben (csak a valódi kiadások), a legnagyobb elöl
export async function getTopExpenses(from: string, to: string, currency: string, limit: number): Promise<TransactionListItem[]> {
  const { data } = await api.get<TransactionListItem[]>('/statistics/top-expenses', { params: { from, to, currency, limit } })
  return data
}
