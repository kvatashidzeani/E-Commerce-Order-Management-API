import { Link } from 'react-router-dom'
import { useAuth } from '@/features/auth/AuthContext'
import { CartItemRow } from '@/features/cart/CartItemRow'
import { useCartQuery } from '@/features/cart/hooks'
import { Button } from '@/components/ui/button'
import { formatMoney } from '@/lib/formatters'

export function CartPage() {
  const { user } = useAuth()
  const cartQuery = useCartQuery(Boolean(user && user.role === 'CUSTOMER'))

  if (cartQuery.isLoading) {
    return <div className="h-40 animate-pulse rounded-lg bg-slate-200" />
  }

  if (cartQuery.isError) {
    return (
      <p className="rounded-md border border-red-200 bg-red-50 p-4 text-red-700">
        Failed to load cart.
      </p>
    )
  }

  const cart = cartQuery.data
  if (!cart || cart.items.length === 0) {
    return (
      <div className="space-y-4 rounded-lg border border-slate-200 bg-white p-8 text-center">
        <h1 className="text-2xl font-semibold">Cart</h1>
        <p className="text-slate-600">Your cart is empty.</p>
        <Link to="/">
          <Button>Browse catalog</Button>
        </Link>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Cart</h1>
        <p className="text-sm text-slate-600">{cart.totalItems} item(s)</p>
      </div>

      <div className="space-y-3">
        {cart.items.map((item) => (
          <CartItemRow key={item.id} item={item} />
        ))}
      </div>

      <div className="flex flex-col gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-lg font-semibold">Total: {formatMoney(cart.totalAmount)}</p>
        <Link to="/checkout">
          <Button>Proceed to checkout</Button>
        </Link>
      </div>
    </div>
  )
}
