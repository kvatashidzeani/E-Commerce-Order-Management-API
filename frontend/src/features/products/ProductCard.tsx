import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import type { Product } from '@/api/types'
import { isApiError } from '@/api/errors'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/features/auth/AuthContext'
import { useAddToCart } from '@/features/cart/hooks'
import { ProductImage } from '@/features/products/ProductImage'
import { formatMoney } from '@/lib/formatters'

type ProductCardProps = {
  product: Product
  index?: number
}

export function ProductCard({ product, index = 0 }: ProductCardProps) {
  const { user } = useAuth()
  const addToCart = useAddToCart()
  const outOfStock = product.stockQuantity <= 0

  const handleAdd = async () => {
    if (!user) {
      toast.error('Please login as a customer to add items')
      return
    }
    if (user.role !== 'CUSTOMER') {
      toast.error('Only customers can use the cart')
      return
    }
    try {
      await addToCart.mutateAsync({ productId: product.id, quantity: 1 })
      toast.success('Added to cart')
    } catch (error) {
      toast.error(isApiError(error) ? error.message : 'Failed to add to cart')
    }
  }

  return (
    <article
      className="animate-rise group flex h-full flex-col overflow-hidden rounded-2xl border border-teal-900/10 bg-white/90 shadow-[0_18px_40px_-28px_rgba(15,70,60,0.45)] transition duration-300 hover:-translate-y-1 hover:shadow-[0_28px_50px_-24px_rgba(15,70,60,0.5)]"
      style={{ animationDelay: `${Math.min(index, 8) * 45}ms` }}
    >
      <div className="relative">
        <ProductImage
          src={product.imageUrl}
          alt={product.name}
          categoryName={product.categoryName}
          className="h-48 w-full"
        />
        <span className="absolute left-3 top-3 rounded-full bg-white/90 px-2.5 py-1 text-[11px] font-medium tracking-wide text-teal-900 shadow-sm backdrop-blur-sm">
          {product.categoryName}
        </span>
      </div>

      <div className="flex flex-1 flex-col gap-3 p-4">
        <div className="space-y-1">
          <h2 className="font-display text-lg font-bold leading-snug text-teal-950">{product.name}</h2>
          <p className="line-clamp-2 text-sm text-slate-600">
            {product.description || 'Quality pick from the Mercato catalog.'}
          </p>
        </div>

        <div className="mt-auto">
          <p className="font-display text-xl font-bold text-teal-900">{formatMoney(product.price)}</p>
          <p className={`text-xs font-medium ${outOfStock ? 'text-rose-600' : 'text-teal-700'}`}>
            {outOfStock ? 'არ არის მარაგში' : `${product.stockQuantity} in stock`}
          </p>
        </div>

        <div className="grid grid-cols-2 gap-2 pt-1">
          <Link to={`/products/${product.id}`} className="block">
            <Button variant="outline" className="w-full">
              Details
            </Button>
          </Link>
          <Button
            className="w-full"
            disabled={outOfStock || addToCart.isPending}
            onClick={() => void handleAdd()}
          >
            {outOfStock ? 'Sold out' : addToCart.isPending ? '...' : 'Add'}
          </Button>
        </div>
      </div>
    </article>
  )
}
