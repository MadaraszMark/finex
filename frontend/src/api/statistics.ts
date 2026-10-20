import { api } from './client'
import type { CategorySpendingResponse, DashboardSummaryResponse, MonthlySummaryResponse } from './types'

// StatisticsController (/statistics): összesítések a bejelentkezett felhasználó számláiról

// Teljes egyenleg, megtakarítások és a folyó hónap bevétele/kiadása egy devizanemben
export async function getSummary(currency: string): Promise<DashboardSummaryResponse> {
  const { data } = await api.get<DashboardSummaryResponse>('/statistics/summary', { params: { currency } })
  return data
}

// Havi bevétel és kiadás az elmúlt hónapokra (a legrégebbi elöl; a forgalom nélküli hónapok is benne vannak)
export async function getMonthlySummary(months: number, currency: string): Promise<MonthlySummaryResponse[]> {
  const { data } = await api.get<MonthlySummaryResponse[]>('/statistics/monthly', { params: { months, currency } })
  return data
}

// Kiadás kategóriánként az időszakban (a from és a to "ÉÉÉÉ-HH-NN", mindkét nap beleszámít), a legnagyobb elöl
export async function getCategorySpending(from: string, to: string, currency: string): Promise<CategorySpendingResponse[]> {
  const { data } = await api.get<CategorySpendingResponse[]>('/statistics/categories', { params: { from, to, currency } })
  return data
}
