import { describe, expect, it } from 'vitest'
import type { OrderStatusHistory } from '@/api/types'

function sortHistory(history: OrderStatusHistory[]): OrderStatusHistory[] {
  return [...history].sort(
    (a, b) => new Date(a.changedAt).getTime() - new Date(b.changedAt).getTime(),
  )
}

describe('StatusTimeline ordering', () => {
  it('orders statuses chronologically', () => {
    const history: OrderStatusHistory[] = [
      {
        fromStatus: 'PENDING',
        toStatus: 'PAID',
        changedByUserId: 1,
        changedAt: '2026-01-02T10:00:00Z',
        comment: null,
      },
      {
        fromStatus: null,
        toStatus: 'PENDING',
        changedByUserId: null,
        changedAt: '2026-01-01T10:00:00Z',
        comment: 'Created',
      },
    ]

    const ordered = sortHistory(history)
    expect(ordered.map((entry) => entry.toStatus)).toEqual(['PENDING', 'PAID'])
  })
})
