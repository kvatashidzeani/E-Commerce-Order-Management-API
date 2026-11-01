import { apiClient } from '@/api/client'
import type { PageResponse, Product } from '@/api/types'

export type ProductListParams = {
  category?: number
  minPrice?: number
  maxPrice?: number
  search?: string
  includeInactive?: boolean
  page?: number
  size?: number
  sort?: string
}

export async function fetchProducts(params: ProductListParams = {}): Promise<PageResponse<Product>> {
  const { data } = await apiClient.get<PageResponse<Product>>('/api/products', { params })
  return data
}

export async function fetchProduct(id: number): Promise<Product> {
  const { data } = await apiClient.get<Product>(`/api/products/${id}`)
  return data
}

export type ProductWritePayload = {
  name: string
  description?: string
  imageUrl?: string
  sku: string
  price: number
  stockQuantity: number
  categoryId: number
  active?: boolean
}

export async function createProduct(payload: ProductWritePayload): Promise<Product> {
  const { data } = await apiClient.post<Product>('/api/products', payload)
  return data
}

export async function updateProduct(
  id: number,
  payload: Partial<ProductWritePayload>,
): Promise<Product> {
  const { data } = await apiClient.put<Product>(`/api/products/${id}`, payload)
  return data
}

export async function deactivateProduct(id: number): Promise<Product> {
  const { data } = await apiClient.delete<Product>(`/api/products/${id}`)
  return data
}
