import type { OrderStatus } from '@/api/types'

const ALLOWED: Record<OrderStatus, OrderStatus[]> = {
  PENDING: ['PAID', 'CANCELLED'],
  PAID: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['DELIVERED'],
  DELIVERED: [],
  CANCELLED: [],
}

export function nextStatuses(current: OrderStatus): OrderStatus[] {
  return ALLOWED[current]
}
