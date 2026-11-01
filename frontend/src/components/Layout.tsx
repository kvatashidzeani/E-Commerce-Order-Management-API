import { Link, NavLink, Outlet } from 'react-router-dom'
import { ShoppingCart } from 'lucide-react'
import { useAuth } from '@/features/auth/AuthContext'
import { Button } from '@/components/ui/button'
import { useCartQuery } from '@/features/cart/hooks'
import { cn } from '@/lib/utils'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  cn(
    'rounded-full px-3 py-1.5 text-sm font-medium transition-colors',
    isActive ? 'bg-teal-800 text-white' : 'text-slate-700 hover:bg-teal-50 hover:text-teal-900',
  )

export function Layout() {
  const { user, logout, isAdmin } = useAuth()
  const cartQuery = useCartQuery(Boolean(user && user.role === 'CUSTOMER'))
  const cartCount = cartQuery.data?.totalItems ?? 0

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-40 border-b border-teal-900/10 bg-white/75 backdrop-blur-md">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
          <Link to="/" className="font-display text-xl font-extrabold tracking-tight text-teal-950">
            Mercato
          </Link>
          <nav className="flex flex-wrap items-center gap-1">
            <NavLink to="/" className={linkClass} end>
              Catalog
            </NavLink>
            {user?.role === 'CUSTOMER' && (
              <>
                <NavLink to="/cart" className={linkClass}>
                  <span className="relative inline-flex items-center gap-1.5">
                    <ShoppingCart className="h-4 w-4" aria-hidden />
                    Cart
                    {cartCount > 0 && (
                      <span className="rounded-full bg-teal-700 px-1.5 text-[10px] text-white">
                        {cartCount}
                      </span>
                    )}
                  </span>
                </NavLink>
                <NavLink to="/orders" className={linkClass}>
                  Orders
                </NavLink>
              </>
            )}
            {isAdmin && (
              <>
                <NavLink to="/admin/products" className={linkClass}>
                  Products
                </NavLink>
                <NavLink to="/admin/orders" className={linkClass}>
                  Admin orders
                </NavLink>
              </>
            )}
          </nav>
          <div className="flex items-center gap-2">
            {user ? (
              <>
                <span className="hidden max-w-40 truncate text-sm text-slate-600 sm:inline">
                  {user.email}
                </span>
                <Button variant="outline" size="sm" onClick={logout}>
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="outline" size="sm">
                    Login
                  </Button>
                </Link>
                <Link to="/register">
                  <Button size="sm">Register</Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
