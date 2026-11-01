import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'
import { fetchAdminOrders, updateOrderStatus } from '@/api/endpoints/orders'
import { isApiError } from '@/api/errors'
import type { OrderStatus } from '@/api/types'
import { Button } from '@/components/ui/button'
import { StatusBadge } from '@/features/orders/StatusBadge'
import { nextStatuses } from '@/features/orders/transitions'
import { orderKeys } from '@/features/orders/orderKeys'
import { formatDateTime, formatMoney } from '@/lib/formatters'

const STATUS_FILTERS: Array<OrderStatus | 'ALL'> = [
  'ALL',
  'PENDING',
  'PAID',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
]

export function AdminOrdersPage() {
  const queryClient = useQueryClient()
  const [params, setParams] = useSearchParams()
  const page = Number(params.get('page') ?? '0')
  const statusParam = params.get('status')
  const status =
    statusParam && STATUS_FILTERS.includes(statusParam as OrderStatus)
      ? (statusParam as OrderStatus)
      : undefined

  const ordersQuery = useQuery({
    queryKey: [...orderKeys.lists(), 'admin', { page, status }],
    queryFn: () => fetchAdminOrders(page, 10, status),
  })

  const statusMutation = useMutation({
    mutationFn: ({ id, next }: { id: number; next: OrderStatus }) =>
      updateOrderStatus(id, next, `Admin set status to ${next}`),
    onSuccess: async () => {
      toast.success('Status updated')
      await queryClient.invalidateQueries({ queryKey: orderKeys.all })
    },
    onError: (error) => toast.error(isApiError(error) ? error.message : 'Status update failed'),
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Admin orders</h1>
        <p className="text-sm text-slate-600">Filter by status and apply allowed transitions.</p>
      </div>

      <div className="flex flex-wrap gap-2">
        {STATUS_FILTERS.map((value) => {
          const active = value === 'ALL' ? !status : status === value
          return (
            <Button
              key={value}
              size="sm"
              variant={active ? 'default' : 'outline'}
              onClick={() => {
                const next = new URLSearchParams(params)
                if (value === 'ALL') next.delete('status')
                else next.set('status', value)
                next.set('page', '0')
                setParams(next)
              }}
            >
              {value}
            </Button>
          )
        })}
      </div>

      {ordersQuery.isLoading && <div className="h-40 animate-pulse rounded-lg bg-slate-200" />}
      {ordersQuery.isError && (
        <p className="rounded-md border border-red-200 bg-red-50 p-4 text-red-700">
          Failed to load orders.
        </p>
      )}

      {ordersQuery.data?.empty && (
        <p className="rounded-md border border-slate-200 bg-white p-6 text-center text-slate-600">
          No orders found.
        </p>
      )}

      {ordersQuery.data && !ordersQuery.data.empty && (
        <>
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="min-w-full text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-slate-600">
                <tr>
                  <th className="px-4 py-3 font-medium">Order</th>
                  <th className="px-4 py-3 font-medium">Date</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium">Total</th>
                  <th className="px-4 py-3 font-medium">Next status</th>
                </tr>
              </thead>
              <tbody>
                {ordersQuery.data.content.map((order) => {
                  const options = nextStatuses(order.status)
                  return (
                    <tr key={order.id} className="border-b border-slate-100 last:border-0">
                      <td className="px-4 py-3 font-medium">{order.orderNumber}</td>
                      <td className="px-4 py-3 text-slate-600">{formatDateTime(order.createdAt)}</td>
                      <td className="px-4 py-3">
                        <StatusBadge status={order.status} />
                      </td>
                      <td className="px-4 py-3">{formatMoney(order.totalAmount)}</td>
                      <td className="px-4 py-3">
                        {options.length === 0 ? (
                          <span className="text-slate-500">—</span>
                        ) : (
                          <select
                            className="h-9 rounded-md border border-slate-300 bg-white px-2 text-sm"
                            defaultValue=""
                            disabled={statusMutation.isPending}
                            onChange={(event) => {
                              const next = event.target.value as OrderStatus
                              if (!next) return
                              void statusMutation.mutateAsync({ id: order.id, next })
                              event.target.value = ''
                            }}
                          >
                            <option value="">Select…</option>
                            {options.map((option) => (
                              <option key={option} value={option}>
                                {option}
                              </option>
                            ))}
                          </select>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
          <div className="flex items-center justify-between">
            <Button
              variant="outline"
              disabled={ordersQuery.data.first}
              onClick={() => {
                const next = new URLSearchParams(params)
                next.set('page', String(Math.max(page - 1, 0)))
                setParams(next)
              }}
            >
              Previous
            </Button>
            <span className="text-sm text-slate-600">
              Page {ordersQuery.data.number + 1} / {ordersQuery.data.totalPages || 1}
            </span>
            <Button
              variant="outline"
              disabled={ordersQuery.data.last}
              onClick={() => {
                const next = new URLSearchParams(params)
                next.set('page', String(page + 1))
                setParams(next)
              }}
            >
              Next
            </Button>
          </div>
        </>
      )}
    </div>
  )
}
