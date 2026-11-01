import { describe, expect, it } from 'vitest'
import type { CartItem } from '@/api/types'

function canSetQuantity(item: CartItem, nextQuantity: number): boolean {
  return nextQuantity >= 1 && nextQuantity <= item.availableStock
}

describe('CartItemRow quantity rules', () => {
  const item: CartItem = {
    id: 1,
    productId: 10,
    productName: 'Phone',
    sku: 'PH-1',
    unitPrice: 10,
    quantity: 2,
    lineTotal: 20,
    availableStock: 3,
  }

  it('allows quantity within stock', () => {
    expect(canSetQuantity(item, 3)).toBe(true)
  })

  it('blocks quantity above stock', () => {
    expect(canSetQuantity(item, 4)).toBe(false)
  })

  it('blocks quantity below one', () => {
    expect(canSetQuantity(item, 0)).toBe(false)
  })
})
