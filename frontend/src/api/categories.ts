import { api } from './client'
import type { CategoryResponse } from './types'

// CategoryController (/categories): a tranzakciókategóriák törzsadata
export async function getCategories(): Promise<CategoryResponse[]> {
  const { data } = await api.get<CategoryResponse[]>('/categories')
  return data
}
