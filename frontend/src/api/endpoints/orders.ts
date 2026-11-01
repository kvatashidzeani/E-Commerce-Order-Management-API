import { apiClient } from '@/api/client'
import type { Order, OrderStatus, OrderSummary, PageResponse, ShippingAddress } from '@/api/types'

export async function checkout(shippingAddress: ShippingAddress): Promise<Order> {
  const { data } = await apiClient.post<Order>('/api/orders', { shippingAddress })
  return data
}

export async function fetchMyOrders(page = 0, size = 20): Promise<PageResponse<OrderSummary>> {
  const { data } = await apiClient.get<PageResponse<OrderSummary>>('/api/orders', {
    params: { page, size },
  })
  return data
}

export async function fetchMyOrder(id: number): Promise<Order> {
  const { data } = await apiClient.get<Order>(`/api/orders/${id}`)
  return data
}

export async function fetchAdminOrders(
  page = 0,
  size = 20,
  status?: OrderStatus,
): Promise<PageResponse<OrderSummary>> {
  const { data } = await apiClient.get<PageResponse<OrderSummary>>('/api/orders/admin/all', {
    params: { page, size, status },
  })
  return data
}

export async function fetchAdminOrder(id: number): Promise<Order> {
  const { data } = await apiClient.get<Order>(`/api/orders/admin/${id}`)
  return data
}

export async function payOrder(id: number): Promise<Order> {
  const { data } = await apiClient.post<Order>(`/api/orders/${id}/pay`)
  return data
}

export async function cancelOrder(id: number): Promise<Order> {
  const { data } = await apiClient.post<Order>(`/api/orders/${id}/cancel`)
  return data
}

export async function updateOrderStatus(
  id: number,
  status: OrderStatus,
  comment?: string,
): Promise<Order> {
  const { data } = await apiClient.patch<Order>(`/api/orders/${id}/status`, { status, comment })
  return data
}
