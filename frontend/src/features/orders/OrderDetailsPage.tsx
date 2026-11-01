import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { toast } from 'sonner'
import { cancelOrder, fetchMyOrder, payOrder } from '@/api/endpoints/orders'
import { isApiError } from '@/api/errors'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { StatusBadge } from '@/features/orders/StatusBadge'
import { StatusTimeline } from '@/features/orders/StatusTimeline'
import { orderKeys } from '@/features/orders/orderKeys'
import { formatDateTime, formatMoney } from '@/lib/formatters'

export function OrderDetailsPage() {
  const { id } = useParams()
  const orderId = Number(id)
  const queryClient = useQueryClient()

  const orderQuery = useQuery({
    queryKey: orderKeys.detail(orderId),
    queryFn: () => fetchMyOrder(orderId),
    enabled: Number.isFinite(orderId),
  })

  const refreshOrders = async () => {
    await queryClient.invalidateQueries({ queryKey: orderKeys.all })
  }

  const payMutation = useMutation({
    mutationFn: () => payOrder(orderId),
    onSuccess: async (order) => {
      queryClient.setQueryData(orderKeys.detail(orderId), order)
      await refreshOrders()
      toast.success('Payment successful')
    },
    onError: (error) => {
      toast.error(isApiError(error) ? error.message : 'Payment failed')
    },
  })

  const cancelMutation = useMutation({
    mutationFn: () => cancelOrder(orderId),
    onSuccess: async (order) => {
      queryClient.setQueryData(orderKeys.detail(orderId), order)
      await refreshOrders()
      toast.success('Order cancelled')
    },
    onError: (error) => {
      toast.error(isApiError(error) ? error.message : 'Cancel failed')
    },
  })

  if (orderQuery.isLoading) {
    return <div className="h-48 animate-pulse rounded-lg bg-slate-200" />
  }

  if (orderQuery.isError || !orderQuery.data) {
    return <p className="text-red-600">Order not found.</p>
  }

  const order = orderQuery.data
  const busy = payMutation.isPending || cancelMutation.isPending
  const canPay = order.status === 'PENDING'
  const canCancel = order.status === 'PENDING' || order.status === 'PAID'

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-sm text-slate-500">
            <Link to="/orders" className="text-teal-800 underline">
              My orders
            </Link>
          </p>
          <h1 className="text-2xl font-semibold">{order.orderNumber}</h1>
          <p className="text-sm text-slate-600">{formatDateTime(order.createdAt)}</p>
        </div>
        <StatusBadge status={order.status} />
      </div>

      <div className="flex flex-wrap gap-2">
        {canPay && (
          <Button disabled={busy} onClick={() => void payMutation.mutateAsync()}>
            {payMutation.isPending ? 'Paying...' : 'Pay'}
          </Button>
        )}
        {canCancel && (
          <Button
            variant="destructive"
            disabled={busy}
            onClick={() => void cancelMutation.mutateAsync()}
          >
            {cancelMutation.isPending ? 'Cancelling...' : 'Cancel order'}
          </Button>
        )}
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Items</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {order.items.map((item) => (
              <div key={item.id} className="flex justify-between gap-3 text-sm">
                <span>
                  {item.productName} × {item.quantity}
                </span>
                <span className="font-medium">{formatMoney(item.lineTotal)}</span>
              </div>
            ))}
            <div className="border-t border-slate-200 pt-3 text-lg font-semibold">
              Total: {formatMoney(order.totalAmount)}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Shipping</CardTitle>
          </CardHeader>
          <CardContent className="space-y-1 text-sm text-slate-700">
            <p>{order.shippingRecipient}</p>
            <p>{order.shippingPhone}</p>
            <p>{order.shippingStreet}</p>
            <p>
              {order.shippingCity}, {order.shippingPostalCode}
            </p>
            <p>{order.shippingCountry}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Status timeline</CardTitle>
        </CardHeader>
        <CardContent>
          <StatusTimeline history={order.statusHistory} />
        </CardContent>
      </Card>
    </div>
  )
}
