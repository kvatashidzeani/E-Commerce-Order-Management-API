import { useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'
import { fetchMyOrders } from '@/api/endpoints/orders'
import { Button } from '@/components/ui/button'
import { StatusBadge } from '@/features/orders/StatusBadge'
import { orderKeys } from '@/features/orders/orderKeys'
import { formatDateTime, formatMoney } from '@/lib/formatters'

export function OrdersPage() {
  const [params, setParams] = useSearchParams()
  const page = Number(params.get('page') ?? '0')

  const ordersQuery = useQuery({
    queryKey: orderKeys.list(page),
    queryFn: () => fetchMyOrders(page, 10),
  })

  if (ordersQuery.isLoading) {
    return <div className="h-40 animate-pulse rounded-lg bg-slate-200" />
  }

  if (ordersQuery.isError) {
    return (
      <p className="rounded-md border border-red-200 bg-red-50 p-4 text-red-700">
        Failed to load orders.
      </p>
    )
  }

  const data = ordersQuery.data
  if (!data || data.empty) {
    return (
      <div className="space-y-4 rounded-lg border border-slate-200 bg-white p-8 text-center">
        <h1 className="text-2xl font-semibold">My orders</h1>
        <p className="text-slate-600">You have no orders yet.</p>
        <Link to="/">
          <Button>Browse catalog</Button>
        </Link>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">My orders</h1>
        <p className="text-sm text-slate-600">{data.totalElements} order(s)</p>
      </div>

      <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="border-b border-slate-200 bg-slate-50 text-slate-600">
            <tr>
              <th className="px-4 py-3 font-medium">Order</th>
              <th className="px-4 py-3 font-medium">Date</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium">Items</th>
              <th className="px-4 py-3 font-medium">Total</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((order) => (
              <tr key={order.id} className="border-b border-slate-100 last:border-0">
                <td className="px-4 py-3">
                  <Link className="font-medium text-teal-800 underline" to={`/orders/${order.id}`}>
                    {order.orderNumber}
                  </Link>
                </td>
                <td className="px-4 py-3 text-slate-600">{formatDateTime(order.createdAt)}</td>
                <td className="px-4 py-3">
                  <StatusBadge status={order.status} />
                </td>
                <td className="px-4 py-3">{order.itemCount}</td>
                <td className="px-4 py-3 font-medium">{formatMoney(order.totalAmount)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between">
        <Button
          variant="outline"
          disabled={data.first}
          onClick={() => {
            const next = new URLSearchParams(params)
            next.set('page', String(Math.max(page - 1, 0)))
            setParams(next)
          }}
        >
          Previous
        </Button>
        <span className="text-sm text-slate-600">
          Page {data.number + 1} / {data.totalPages || 1}
        </span>
        <Button
          variant="outline"
          disabled={data.last}
          onClick={() => {
            const next = new URLSearchParams(params)
            next.set('page', String(page + 1))
            setParams(next)
          }}
        >
          Next
        </Button>
      </div>
    </div>
  )
}
