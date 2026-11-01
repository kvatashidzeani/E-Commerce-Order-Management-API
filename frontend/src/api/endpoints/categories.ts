import { apiClient } from '@/api/client'
import type { Category } from '@/api/types'

export async function fetchCategories(): Promise<Category[]> {
  const { data } = await apiClient.get<Category[]>('/api/categories')
  return data
}
