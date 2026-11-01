import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  addCartItem,
  fetchCart,
  removeCartItem,
  updateCartItem,
} from '@/api/endpoints/cart'
import type { Cart } from '@/api/types'

export const cartKeys = {
  all: ['cart'] as const,
}

export function useCartQuery(enabled: boolean) {
  return useQuery({
    queryKey: cartKeys.all,
    queryFn: fetchCart,
    enabled,
  })
}

export function useAddToCart() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ productId, quantity }: { productId: number; quantity: number }) =>
      addCartItem(productId, quantity),
    onSuccess: (cart) => {
      queryClient.setQueryData(cartKeys.all, cart)
    },
  })
}

export function useUpdateCartItem() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ itemId, quantity }: { itemId: number; quantity: number }) =>
      updateCartItem(itemId, quantity),
    onMutate: async ({ itemId, quantity }) => {
      await queryClient.cancelQueries({ queryKey: cartKeys.all })
      const previous = queryClient.getQueryData<Cart>(cartKeys.all)

      if (previous) {
        const items = previous.items.map((item) =>
          item.id === itemId
            ? {
                ...item,
                quantity,
                lineTotal: Number(item.unitPrice) * quantity,
              }
            : item,
        )
        const totalItems = items.reduce((sum, item) => sum + item.quantity, 0)
        const totalAmount = items.reduce((sum, item) => sum + Number(item.lineTotal), 0)
        queryClient.setQueryData<Cart>(cartKeys.all, {
          ...previous,
          items,
          totalItems,
          totalAmount,
        })
      }

      return { previous }
    },
    onError: (_error, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(cartKeys.all, context.previous)
      }
    },
    onSettled: () => {
      void queryClient.invalidateQueries({ queryKey: cartKeys.all })
    },
  })
}

export function useRemoveCartItem() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (itemId: number) => removeCartItem(itemId),
    onMutate: async (itemId) => {
      await queryClient.cancelQueries({ queryKey: cartKeys.all })
      const previous = queryClient.getQueryData<Cart>(cartKeys.all)

      if (previous) {
        const items = previous.items.filter((item) => item.id !== itemId)
        const totalItems = items.reduce((sum, item) => sum + item.quantity, 0)
        const totalAmount = items.reduce((sum, item) => sum + Number(item.lineTotal), 0)
        queryClient.setQueryData<Cart>(cartKeys.all, {
          ...previous,
          items,
          totalItems,
          totalAmount,
        })
      }

      return { previous }
    },
    onError: (_error, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(cartKeys.all, context.previous)
      }
    },
    onSettled: () => {
      void queryClient.invalidateQueries({ queryKey: cartKeys.all })
    },
  })
}
