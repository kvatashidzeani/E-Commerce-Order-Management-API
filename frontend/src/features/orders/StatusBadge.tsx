import type { OrderStatus } from '@/api/types'
import { cn } from '@/lib/utils'

const styles: Record<OrderStatus, string> = {
  PENDING: 'bg-amber-100 text-amber-800',
  PAID: 'bg-sky-100 text-sky-800',
  SHIPPED: 'bg-indigo-100 text-indigo-800',
  DELIVERED: 'bg-emerald-100 text-emerald-800',
  CANCELLED: 'bg-slate-200 text-slate-700',
}

type StatusBadgeProps = {
  status: OrderStatus
}

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <span className={cn('inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium', styles[status])}>
      {status}
    </span>
  )
}
