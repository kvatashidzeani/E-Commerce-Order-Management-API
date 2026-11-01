import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { checkout } from '@/api/endpoints/orders'
import { isApiError } from '@/api/errors'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { cartKeys, useCartQuery } from '@/features/cart/hooks'
import { AddressForm } from '@/features/checkout/AddressForm'
import { addressSchema, type AddressFormValues } from '@/features/checkout/addressSchema'
import { orderKeys } from '@/features/orders/orderKeys'
import { formatMoney } from '@/lib/formatters'

export function CheckoutPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const cartQuery = useCartQuery(true)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<AddressFormValues>({
    resolver: zodResolver(addressSchema),
    defaultValues: {
      recipientName: '',
      phone: '',
      country: '',
      city: '',
      street: '',
      postalCode: '',
    },
  })

  const checkoutMutation = useMutation({
    mutationFn: checkout,
    onSuccess: async (order) => {
      await queryClient.invalidateQueries({ queryKey: cartKeys.all })
      await queryClient.invalidateQueries({ queryKey: orderKeys.all })
      toast.success('Order placed')
      navigate(`/orders/${order.id}`)
    },
    onError: async (error) => {
      if (isApiError(error)) {
        Object.entries(error.fieldErrors).forEach(([field, message]) => {
          if (field in addressSchema.shape) {
            setError(field as keyof AddressFormValues, { message })
          }
        })
        if (error.status === 409) {
          toast.error(error.message || 'Stock conflict — please review your cart')
          await queryClient.invalidateQueries({ queryKey: cartKeys.all })
          return
        }
        toast.error(error.message)
        return
      }
      toast.error('Checkout failed')
    },
  })

  if (cartQuery.isLoading) {
    return <div className="h-40 animate-pulse rounded-lg bg-slate-200" />
  }

  if (cartQuery.isError) {
    return (
      <p className="rounded-md border border-red-200 bg-red-50 p-4 text-red-700">
        Failed to load cart for checkout.
      </p>
    )
  }

  const cart = cartQuery.data
  if (!cart || cart.items.length === 0) {
    return (
      <div className="space-y-4 rounded-lg border border-slate-200 bg-white p-8 text-center">
        <h1 className="text-2xl font-semibold">Checkout</h1>
        <p className="text-slate-600">Your cart is empty.</p>
        <Link to="/">
          <Button>Browse catalog</Button>
        </Link>
      </div>
    )
  }

  const busy = isSubmitting || checkoutMutation.isPending

  const onSubmit = handleSubmit((values) => {
    checkoutMutation.mutate(values)
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Checkout</h1>
        <p className="text-sm text-slate-600">Confirm shipping details and place your order.</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-5">
        <Card className="lg:col-span-3">
          <CardHeader>
            <CardTitle>Shipping address</CardTitle>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={onSubmit} noValidate>
              <AddressForm register={register} errors={errors} />
              <Button type="submit" disabled={busy} className="w-full sm:w-auto">
                {busy ? 'Placing order...' : 'Place order'}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Order summary</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {cart.items.map((item) => (
              <div key={item.id} className="flex justify-between gap-3 text-sm">
                <span>
                  {item.productName} × {item.quantity}
                </span>
                <span className="font-medium">{formatMoney(item.lineTotal)}</span>
              </div>
            ))}
            <div className="border-t border-slate-200 pt-3 text-lg font-semibold">
              Total: {formatMoney(cart.totalAmount)}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
