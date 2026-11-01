import { apiClient } from '@/api/client'
import type { Cart } from '@/api/types'

export async function fetchCart(): Promise<Cart> {
  const { data } = await apiClient.get<Cart>('/api/cart')
  return data
}

export async function addCartItem(productId: number, quantity: number): Promise<Cart> {
  const { data } = await apiClient.post<Cart>('/api/cart/items', { productId, quantity })
  return data
}

export async function updateCartItem(itemId: number, quantity: number): Promise<Cart> {
  const { data } = await apiClient.patch<Cart>(`/api/cart/items/${itemId}`, { quantity })
  return data
}

export async function removeCartItem(itemId: number): Promise<Cart> {
  const { data } = await apiClient.delete<Cart>(`/api/cart/items/${itemId}`)
  return data
}
