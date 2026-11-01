import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { fetchCategories } from '@/api/endpoints/categories'
import type { Product } from '@/api/types'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

const schema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(255),
  description: z.string().max(5000).optional(),
  imageUrl: z
    .string()
    .trim()
    .max(500)
    .optional()
    .or(z.literal(''))
    .refine((value) => !value || /^https?:\/\//i.test(value), 'Enter a valid http(s) URL'),
  sku: z.string().trim().min(1, 'SKU is required').max(100),
  price: z.number().min(0, 'Price must be ≥ 0'),
  stockQuantity: z.number().int().min(0, 'Stock must be ≥ 0'),
  categoryId: z.number().int().positive('Category is required'),
  active: z.boolean(),
})

export type ProductFormValues = z.infer<typeof schema>

type ProductFormProps = {
  product?: Product | null
  submitting?: boolean
  onSubmit: (values: ProductFormValues) => Promise<void> | void
  onCancel: () => void
}

export function ProductForm({ product, submitting, onSubmit, onCancel }: ProductFormProps) {
  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
  })

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ProductFormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: product?.name ?? '',
      description: product?.description ?? '',
      imageUrl: product?.imageUrl ?? '',
      sku: product?.sku ?? '',
      price: product ? Number(product.price) : 0,
      stockQuantity: product?.stockQuantity ?? 0,
      categoryId: product?.categoryId ?? 0,
      active: product?.active ?? true,
    },
  })

  return (
    <form className="space-y-4" onSubmit={handleSubmit((values) => void onSubmit(values))} noValidate>
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="space-y-1.5">
          <Label htmlFor="name">Name</Label>
          <Input id="name" {...register('name')} />
          {errors.name && <p className="text-sm text-red-600">{errors.name.message}</p>}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="sku">SKU</Label>
          <Input id="sku" disabled={Boolean(product)} {...register('sku')} />
          {errors.sku && <p className="text-sm text-red-600">{errors.sku.message}</p>}
        </div>
        <div className="space-y-1.5 sm:col-span-2">
          <Label htmlFor="description">Description</Label>
          <Input id="description" {...register('description')} />
          {errors.description && <p className="text-sm text-red-600">{errors.description.message}</p>}
        </div>
        <div className="space-y-1.5 sm:col-span-2">
          <Label htmlFor="imageUrl">Image URL</Label>
          <Input id="imageUrl" placeholder="https://..." {...register('imageUrl')} />
          {errors.imageUrl && <p className="text-sm text-red-600">{errors.imageUrl.message}</p>}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="price">Price</Label>
          <Input
            id="price"
            type="number"
            step="0.01"
            min={0}
            {...register('price', { valueAsNumber: true })}
          />
          {errors.price && <p className="text-sm text-red-600">{errors.price.message}</p>}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="stockQuantity">Stock</Label>
          <Input
            id="stockQuantity"
            type="number"
            min={0}
            {...register('stockQuantity', { valueAsNumber: true })}
          />
          {errors.stockQuantity && (
            <p className="text-sm text-red-600">{errors.stockQuantity.message}</p>
          )}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="categoryId">Category</Label>
          <select
            id="categoryId"
            className="flex h-10 w-full rounded-md border border-slate-300 bg-white px-3 text-sm"
            {...register('categoryId', { valueAsNumber: true })}
          >
            <option value={0}>Select category</option>
            {categoriesQuery.data?.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
          {errors.categoryId && <p className="text-sm text-red-600">{errors.categoryId.message}</p>}
        </div>
        <div className="flex items-end gap-2 pb-2">
          <input id="active" type="checkbox" className="h-4 w-4" {...register('active')} />
          <Label htmlFor="active">Active</Label>
        </div>
      </div>
      <div className="flex justify-end gap-2">
        <Button type="button" variant="outline" onClick={onCancel} disabled={submitting}>
          Cancel
        </Button>
        <Button type="submit" disabled={submitting}>
          {submitting ? 'Saving...' : product ? 'Save changes' : 'Create product'}
        </Button>
      </div>
    </form>
  )
}
