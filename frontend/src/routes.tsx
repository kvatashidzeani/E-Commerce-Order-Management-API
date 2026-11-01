import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from '@/components/Layout'
import { NotFoundPage } from '@/components/NotFoundPage'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { LoginPage } from '@/features/auth/LoginPage'
import { RegisterPage } from '@/features/auth/RegisterPage'
import { ProductListPage } from '@/features/products/ProductListPage'
import { ProductDetailsPage } from '@/features/products/ProductDetailsPage'
import { CartPage } from '@/features/cart/CartPage'
import { CheckoutPage } from '@/features/checkout/CheckoutPage'
import { OrdersPage } from '@/features/orders/OrdersPage'
import { OrderDetailsPage } from '@/features/orders/OrderDetailsPage'
import { AdminProductsPage } from '@/features/admin/AdminProductsPage'
import { AdminOrdersPage } from '@/features/admin/AdminOrdersPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<ProductListPage />} />
        <Route path="products/:id" element={<ProductDetailsPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        <Route element={<ProtectedRoute requiredRole="CUSTOMER" />}>
          <Route path="cart" element={<CartPage />} />
          <Route path="checkout" element={<CheckoutPage />} />
          <Route path="orders" element={<OrdersPage />} />
          <Route path="orders/:id" element={<OrderDetailsPage />} />
        </Route>

        <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
          <Route path="admin/products" element={<AdminProductsPage />} />
          <Route path="admin/orders" element={<AdminOrdersPage />} />
          <Route path="admin" element={<Navigate to="/admin/orders" replace />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
