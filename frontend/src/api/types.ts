export type Role = 'CUSTOMER' | 'ADMIN'

export type AuthResponse = {
  accessToken: string
  tokenType: string
  userId: number
  email: string
  firstName: string
  lastName: string
  role: Role
}

export type AuthUser = {
  id: number
  email: string
  firstName: string
  lastName: string
  role: Role
}

export type Product = {
  id: number
  name: string
  description: string | null
  imageUrl: string | null
  sku: string
  price: number | string
  stockQuantity: number
  categoryId: number
  categoryName: string
  active: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export type PageResponse<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
  empty: boolean
}

export type Category = {
  id: number
  name: string
  parentId: number | null
}

export type CartItem = {
  id: number
  productId: number
  productName: string
  sku: string
  unitPrice: number | string
  quantity: number
  lineTotal: number | string
  availableStock: number
}

export type Cart = {
  id: number
  items: CartItem[]
  totalAmount: number | string
  totalItems: number
}

export type OrderStatus = 'PENDING' | 'PAID' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'

export type OrderSummary = {
  id: number
  orderNumber: string
  status: OrderStatus
  totalAmount: number | string
  itemCount: number
  createdAt: string
}

export type OrderItem = {
  id: number
  productId: number
  productName: string
  unitPriceAtPurchase: number | string
  quantity: number
  lineTotal: number | string
}

export type OrderStatusHistory = {
  fromStatus: OrderStatus | null
  toStatus: OrderStatus
  changedByUserId: number | null
  changedAt: string
  comment: string | null
}

export type Order = {
  id: number
  orderNumber: string
  status: OrderStatus
  totalAmount: number | string
  shippingRecipient: string
  shippingPhone: string
  shippingCountry: string
  shippingCity: string
  shippingStreet: string
  shippingPostalCode: string
  items: OrderItem[]
  statusHistory: OrderStatusHistory[]
  paidAt: string | null
  shippedAt: string | null
  deliveredAt: string | null
  cancelledAt: string | null
  createdAt: string
}

export type ShippingAddress = {
  recipientName: string
  phone: string
  country: string
  city: string
  street: string
  postalCode: string
}
