import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { fetchProducts } from '@/api/endpoints/products'
import { Button } from '@/components/ui/button'
import { ProductCard } from '@/features/products/ProductCard'
import { ProductFilters } from '@/features/products/ProductFilters'
import { productKeys } from '@/features/products/productKeys'

export function ProductListPage() {
  const [params, setParams] = useSearchParams()
  const search = params.get('search') ?? undefined
  const category = params.get('category') ? Number(params.get('category')) : undefined
  const minPrice = params.get('minPrice') ? Number(params.get('minPrice')) : undefined
  const maxPrice = params.get('maxPrice') ? Number(params.get('maxPrice')) : undefined
  const page = Number(params.get('page') ?? '0')
  const sort = params.get('sort') ?? 'name,asc'

  const filters = {
    search,
    category: Number.isFinite(category) ? category : undefined,
    minPrice: Number.isFinite(minPrice) ? minPrice : undefined,
    maxPrice: Number.isFinite(maxPrice) ? maxPrice : undefined,
    page,
    size: 12,
    sort,
  }

  const productsQuery = useQuery({
    queryKey: productKeys.list(filters),
    queryFn: () => fetchProducts(filters),
  })

  return (
    <div className="space-y-8">
      <section className="relative overflow-hidden rounded-[1.75rem] border border-teal-900/10 text-teal-50">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_15%_20%,rgba(45,212,191,0.35),transparent_42%),radial-gradient(circle_at_85%_10%,rgba(56,189,248,0.22),transparent_35%),linear-gradient(120deg,#042f2e,#0f766e_55%,#115e59)]" />
        <div className="absolute -right-8 top-6 h-48 w-48 animate-float rounded-full bg-teal-200/25 blur-3xl" />
        <div className="absolute bottom-0 right-0 h-40 w-[55%] bg-[linear-gradient(120deg,transparent,rgba(255,255,255,0.08))]" />
        <div className="relative space-y-4 px-6 py-12 sm:px-10 sm:py-14">
          <p className="animate-rise text-sm font-medium uppercase tracking-[0.22em] text-teal-200/90">
            Mercato
          </p>
          <h1 className="animate-rise font-display max-w-2xl text-4xl font-extrabold leading-tight sm:text-5xl">
            Curated goods for everyday living
          </h1>
          <p className="animate-rise max-w-xl text-base text-teal-100/90" style={{ animationDelay: '80ms' }}>
            Laptops, phones, books, clothing, food, home and sports — every aisle filled and ready to
            order.
          </p>
        </div>
      </section>

      <ProductFilters />

      {productsQuery.isLoading && (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <div key={index} className="h-80 animate-pulse rounded-2xl bg-teal-900/10" />
          ))}
        </div>
      )}

      {productsQuery.isError && (
        <p className="rounded-2xl border border-red-200 bg-red-50 p-4 text-red-700">
          Failed to load products. Is the backend running on {import.meta.env.VITE_API_URL}?
        </p>
      )}

      {productsQuery.data?.empty && (
        <p className="rounded-2xl border border-teal-900/10 bg-white/80 p-8 text-center text-slate-600">
          პროდუქტი ვერ მოიძებნა
        </p>
      )}

      {productsQuery.data && !productsQuery.data.empty && (
        <>
          <div className="flex items-center justify-between gap-3">
            <p className="text-sm text-slate-600">
              Showing {productsQuery.data.content.length} of {productsQuery.data.totalElements} products
            </p>
          </div>
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {productsQuery.data.content.map((product, index) => (
              <ProductCard key={product.id} product={product} index={index} />
            ))}
          </div>
          <div className="flex items-center justify-between">
            <Button
              variant="outline"
              disabled={productsQuery.data.first}
              onClick={() => {
                const next = new URLSearchParams(params)
                next.set('page', String(Math.max(page - 1, 0)))
                setParams(next)
              }}
            >
              Previous
            </Button>
            <span className="text-sm text-slate-600">
              Page {productsQuery.data.number + 1} / {productsQuery.data.totalPages || 1}
            </span>
            <Button
              variant="outline"
              disabled={productsQuery.data.last}
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
