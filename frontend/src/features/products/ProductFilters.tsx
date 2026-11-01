import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { fetchCategories } from '@/api/endpoints/categories'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { categoryTheme } from '@/features/products/categoryTheme'
import { useDebounce } from '@/lib/useDebounce'
import { cn } from '@/lib/utils'

export function ProductFilters() {
  const [params, setParams] = useSearchParams()
  const [searchInput, setSearchInput] = useState(params.get('search') ?? '')
  const debouncedSearch = useDebounce(searchInput, 300)
  const selectedCategory = params.get('category') ?? ''

  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
  })

  useEffect(() => {
    const current = params.get('search') ?? ''
    if (debouncedSearch === current) return
    const next = new URLSearchParams(params)
    if (debouncedSearch) next.set('search', debouncedSearch)
    else next.delete('search')
    next.set('page', '0')
    setParams(next)
  }, [debouncedSearch, params, setParams])

  const updateParam = (key: string, value: string) => {
    const next = new URLSearchParams(params)
    if (value) next.set(key, value)
    else next.delete(key)
    next.set('page', '0')
    setParams(next)
  }

  const clearFilters = () => {
    setSearchInput('')
    setParams(new URLSearchParams())
  }

  return (
    <div className="space-y-4">
      <div className="flex gap-2 overflow-x-auto pb-1">
        <button
          type="button"
          onClick={() => updateParam('category', '')}
          className={cn(
            'shrink-0 rounded-full border px-4 py-2 text-sm font-medium transition',
            !selectedCategory
              ? 'border-teal-800 bg-teal-800 text-white'
              : 'border-teal-900/15 bg-white/80 text-slate-700 hover:border-teal-700/40',
          )}
        >
          All
        </button>
        {categoriesQuery.data?.map((category) => {
          const theme = categoryTheme(category.name)
          const active = selectedCategory === String(category.id)
          return (
            <button
              key={category.id}
              type="button"
              onClick={() => updateParam('category', String(category.id))}
              className={cn(
                'shrink-0 rounded-full border px-4 py-2 text-sm font-medium transition',
                active ? 'text-white shadow-sm' : 'bg-white/80 text-slate-700 hover:brightness-95',
              )}
              style={
                active
                  ? { background: theme.accent, borderColor: theme.accent }
                  : { borderColor: `${theme.accent}33`, color: theme.ink }
              }
            >
              {category.name}
            </button>
          )
        })}
      </div>

      <div className="grid gap-3 rounded-2xl border border-teal-900/10 bg-white/85 p-4 shadow-[0_16px_40px_-30px_rgba(15,70,60,0.5)] sm:grid-cols-2 lg:grid-cols-5">
        <div className="space-y-1.5 lg:col-span-2">
          <Label htmlFor="search">Search</Label>
          <Input
            id="search"
            placeholder="Search by name"
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="minPrice">Min price</Label>
          <Input
            id="minPrice"
            type="number"
            min={0}
            step="0.01"
            value={params.get('minPrice') ?? ''}
            onChange={(event) => updateParam('minPrice', event.target.value)}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="maxPrice">Max price</Label>
          <Input
            id="maxPrice"
            type="number"
            min={0}
            step="0.01"
            value={params.get('maxPrice') ?? ''}
            onChange={(event) => updateParam('maxPrice', event.target.value)}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="sort">Sort</Label>
          <select
            id="sort"
            className="flex h-10 w-full rounded-md border border-slate-300 bg-white px-3 text-sm"
            value={params.get('sort') ?? 'name,asc'}
            onChange={(event) => updateParam('sort', event.target.value)}
          >
            <option value="name,asc">Name A-Z</option>
            <option value="name,desc">Name Z-A</option>
            <option value="price,asc">Price low-high</option>
            <option value="price,desc">Price high-low</option>
          </select>
        </div>
        <div className="flex items-end lg:col-span-5">
          <Button type="button" variant="outline" onClick={clearFilters}>
            Clear filters
          </Button>
        </div>
      </div>
    </div>
  )
}
