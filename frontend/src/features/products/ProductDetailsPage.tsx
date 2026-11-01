import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { toast } from 'sonner'
import { fetchProduct } from '@/api/endpoints/products'
import { isApiError } from '@/api/errors'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/features/auth/AuthContext'
import { useAddToCart } from '@/features/cart/hooks'
import { ProductImage } from '@/features/products/ProductImage'
import { productKeys } from '@/features/products/productKeys'
import { formatMoney } from '@/lib/formatters'

export function ProductDetailsPage() {
  const { id } = useParams()
  const productId = Number(id)
  const { user } = useAuth()
  const addToCart = useAddToCart()
  const [quantity, setQuantity] = useState(1)

  const productQuery = useQuery({
    queryKey: productKeys.detail(productId),
    queryFn: () => fetchProduct(productId),
    enabled: Number.isFinite(productId),
  })

  if (productQuery.isLoading) {
    return <div className="h-72 animate-pulse rounded-2xl bg-teal-900/10" />
  }

  if (productQuery.isError || !productQuery.data) {
    return <p className="text-red-600">Product not found.</p>
  }

  const product = productQuery.data
  const outOfStock = product.stockQuantity <= 0
  const maxQty = Math.max(product.stockQuantity, 1)

  const handleAdd = async () => {
    if (!user || user.role !== 'CUSTOMER') {
      toast.error('Please login as a customer')
      return
    }
    try {
      await addToCart.mutateAsync({ productId: product.id, quantity })
      toast.success('Added to cart')
    } catch (error) {
      toast.error(isApiError(error) ? error.message : 'Failed to add to cart')
    }
  }

  return (
    <div className="animate-rise overflow-hidden rounded-[1.75rem] border border-teal-900/10 bg-white/90 shadow-[0_24px_60px_-36px_rgba(15,70,60,0.55)]">
      <div className="grid lg:grid-cols-2">
        <ProductImage
          src={product.imageUrl}
          alt={product.name}
          categoryName={product.categoryName}
          className="min-h-72 w-full lg:min-h-full"
          imgClassName="min-h-72 lg:min-h-[28rem]"
        />

        <div className="flex flex-col gap-6 p-6 sm:p-10">
          <div className="space-y-3">
            <p className="text-sm font-medium uppercase tracking-wide text-teal-700">
              {product.categoryName}
            </p>
            <h1 className="font-display text-3xl font-extrabold text-teal-950 sm:text-4xl">
              {product.name}
            </h1>
            <p className="font-display text-3xl font-bold text-teal-900">{formatMoney(product.price)}</p>
            <p className="leading-relaxed text-slate-700">
              {product.description || 'Quality pick from the Mercato catalog.'}
            </p>
            <p className={`text-sm font-medium ${outOfStock ? 'text-rose-600' : 'text-teal-700'}`}>
              {outOfStock ? 'არ არის მარაგში' : `${product.stockQuantity} units available`}
            </p>
            <p className="text-xs text-slate-500">SKU {product.sku}</p>
          </div>

          <div className="mt-auto space-y-4 rounded-2xl border border-teal-900/10 bg-teal-50/50 p-5">
            <div className="space-y-1.5">
              <Label htmlFor="quantity">Quantity</Label>
              <Input
                id="quantity"
                type="number"
                min={1}
                max={maxQty}
                value={quantity}
                disabled={outOfStock}
                onChange={(event) => {
                  const value = Number(event.target.value)
                  if (!Number.isFinite(value)) return
                  setQuantity(Math.min(Math.max(1, value), product.stockQuantity || 1))
                }}
              />
            </div>
            <div className="flex flex-col gap-2">
              <Button disabled={outOfStock || addToCart.isPending} onClick={() => void handleAdd()}>
                {outOfStock ? 'Out of stock' : addToCart.isPending ? 'Adding...' : 'Add to cart'}
              </Button>
              <Link to="/">
                <Button variant="outline" className="w-full">
                  Back to catalog
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
