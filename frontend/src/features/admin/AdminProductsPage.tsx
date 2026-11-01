import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { toast } from 'sonner'
import {
  createProduct,
  deactivateProduct,
  fetchProducts,
  updateProduct,
} from '@/api/endpoints/products'
import { isApiError } from '@/api/errors'
import type { Product } from '@/api/types'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { ProductForm, type ProductFormValues } from '@/features/admin/ProductForm'
import { productKeys } from '@/features/products/productKeys'
import { formatMoney } from '@/lib/formatters'
import { useDebounce } from '@/lib/useDebounce'

export function AdminProductsPage() {
  const queryClient = useQueryClient()
  const [params, setParams] = useSearchParams()
  const page = Number(params.get('page') ?? '0')
  const [searchInput, setSearchInput] = useState(params.get('search') ?? '')
  const search = useDebounce(searchInput, 300)
  const [editing, setEditing] = useState<Product | null>(null)
  const [creating, setCreating] = useState(false)

  const filters = {
    search: search || undefined,
    includeInactive: true,
    page,
    size: 10,
    sort: 'name,asc',
  }

  const productsQuery = useQuery({
    queryKey: productKeys.list(filters),
    queryFn: () => fetchProducts(filters),
  })

  const invalidate = async () => {
    await queryClient.invalidateQueries({ queryKey: productKeys.all })
  }

  const createMutation = useMutation({
    mutationFn: createProduct,
    onSuccess: async () => {
      toast.success('Product created')
      setCreating(false)
      await invalidate()
    },
    onError: (error) => toast.error(isApiError(error) ? error.message : 'Create failed'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, values }: { id: number; values: ProductFormValues }) =>
      updateProduct(id, {
        name: values.name,
        description: values.description,
        imageUrl: values.imageUrl || undefined,
        price: values.price,
        stockQuantity: values.stockQuantity,
        categoryId: values.categoryId,
        active: values.active,
      }),
    onSuccess: async () => {
      toast.success('Product updated')
      setEditing(null)
      await invalidate()
    },
    onError: (error) => toast.error(isApiError(error) ? error.message : 'Update failed'),
  })

  const toggleMutation = useMutation({
    mutationFn: async (product: Product) => {
      if (product.active) {
        return deactivateProduct(product.id)
      }
      return updateProduct(product.id, { active: true })
    },
    onSuccess: async (product) => {
      toast.success(product.active ? 'Product activated' : 'Product deactivated')
      await invalidate()
    },
    onError: (error) => toast.error(isApiError(error) ? error.message : 'Toggle failed'),
  })

  const handleCreate = async (values: ProductFormValues) => {
    await createMutation.mutateAsync({
      name: values.name,
      description: values.description,
      imageUrl: values.imageUrl || undefined,
      sku: values.sku,
      price: values.price,
      stockQuantity: values.stockQuantity,
      categoryId: values.categoryId,
      active: values.active,
    })
  }

  const handleUpdate = async (values: ProductFormValues) => {
    if (!editing) return
    await updateMutation.mutateAsync({ id: editing.id, values })
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">Admin products</h1>
          <p className="text-sm text-slate-600">Create, edit and activate/deactivate catalog items.</p>
        </div>
        <Button
          onClick={() => {
            setEditing(null)
            setCreating(true)
          }}
        >
          New product
        </Button>
      </div>

      <div className="max-w-sm">
        <Input
          placeholder="Search products"
          value={searchInput}
          onChange={(event) => {
            setSearchInput(event.target.value)
            const next = new URLSearchParams(params)
            if (event.target.value) next.set('search', event.target.value)
            else next.delete('search')
            next.set('page', '0')
            setParams(next)
          }}
        />
      </div>

      {(creating || editing) && (
        <Card>
          <CardHeader>
            <CardTitle>{editing ? `Edit ${editing.name}` : 'Create product'}</CardTitle>
          </CardHeader>
          <CardContent>
            <ProductForm
              key={editing?.id ?? 'new'}
              product={editing}
              submitting={createMutation.isPending || updateMutation.isPending}
              onSubmit={editing ? handleUpdate : handleCreate}
              onCancel={() => {
                setCreating(false)
                setEditing(null)
              }}
            />
          </CardContent>
        </Card>
      )}

      {productsQuery.isLoading && <div className="h-40 animate-pulse rounded-lg bg-slate-200" />}
      {productsQuery.isError && (
        <p className="rounded-md border border-red-200 bg-red-50 p-4 text-red-700">
          Failed to load products.
        </p>
      )}

      {productsQuery.data && (
        <>
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="min-w-full text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-slate-600">
                <tr>
                  <th className="px-4 py-3 font-medium">Name</th>
                  <th className="px-4 py-3 font-medium">SKU</th>
                  <th className="px-4 py-3 font-medium">Price</th>
                  <th className="px-4 py-3 font-medium">Stock</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {productsQuery.data.content.map((product) => (
                  <tr key={product.id} className="border-b border-slate-100 last:border-0">
                    <td className="px-4 py-3 font-medium">{product.name}</td>
                    <td className="px-4 py-3 text-slate-600">{product.sku}</td>
                    <td className="px-4 py-3">{formatMoney(product.price)}</td>
                    <td className="px-4 py-3">{product.stockQuantity}</td>
                    <td className="px-4 py-3">{product.active ? 'Active' : 'Inactive'}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => {
                            setCreating(false)
                            setEditing(product)
                          }}
                        >
                          Edit
                        </Button>
                        <Button
                          size="sm"
                          variant={product.active ? 'destructive' : 'secondary'}
                          disabled={toggleMutation.isPending}
                          onClick={() => void toggleMutation.mutateAsync(product)}
                        >
                          {product.active ? 'Deactivate' : 'Activate'}
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
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
