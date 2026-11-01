import { toast } from 'sonner'
import type { CartItem } from '@/api/types'
import { isApiError } from '@/api/errors'
import { Button } from '@/components/ui/button'
import { useRemoveCartItem, useUpdateCartItem } from '@/features/cart/hooks'
import { formatMoney } from '@/lib/formatters'

type CartItemRowProps = {
  item: CartItem
}

export function CartItemRow({ item }: CartItemRowProps) {
  const updateItem = useUpdateCartItem()
  const removeItem = useRemoveCartItem()
  const busy = updateItem.isPending || removeItem.isPending

  const changeQuantity = async (nextQuantity: number) => {
    if (nextQuantity < 1) return
    if (nextQuantity > item.availableStock) {
      toast.error(`Only ${item.availableStock} in stock`)
      return
    }
    try {
      await updateItem.mutateAsync({ itemId: item.id, quantity: nextQuantity })
    } catch (error) {
      toast.error(isApiError(error) ? error.message : 'Failed to update quantity')
    }
  }

  const handleRemove = async () => {
    try {
      await removeItem.mutateAsync(item.id)
      toast.success('Item removed')
    } catch (error) {
      toast.error(isApiError(error) ? error.message : 'Failed to remove item')
    }
  }

  return (
    <div className="flex flex-col gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p className="font-medium">{item.productName}</p>
        <p className="text-sm text-slate-500">{item.sku}</p>
        <p className="text-sm text-slate-600">{formatMoney(item.unitPrice)} each</p>
      </div>
      <div className="flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={busy || item.quantity <= 1}
            onClick={() => void changeQuantity(item.quantity - 1)}
            aria-label="Decrease quantity"
          >
            −
          </Button>
          <span className="min-w-8 text-center text-sm font-medium">{item.quantity}</span>
          <Button
            variant="outline"
            size="sm"
            disabled={busy || item.quantity >= item.availableStock}
            onClick={() => void changeQuantity(item.quantity + 1)}
            aria-label="Increase quantity"
          >
            +
          </Button>
        </div>
        <p className="min-w-20 text-right font-semibold">{formatMoney(item.lineTotal)}</p>
        <Button variant="destructive" size="sm" disabled={busy} onClick={() => void handleRemove()}>
          Remove
        </Button>
      </div>
    </div>
  )
}
