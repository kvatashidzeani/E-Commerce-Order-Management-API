import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/AuthContext'
import type { Role } from '@/api/types'

type ProtectedRouteProps = {
  requiredRole?: Role
}

export function ProtectedRoute({ requiredRole }: ProtectedRouteProps) {
  const { user, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center text-slate-500">Loading...</div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (requiredRole && user.role !== requiredRole) {
    return (
      <div className="mx-auto max-w-lg p-8 text-center">
        <h1 className="text-2xl font-semibold">403 Forbidden</h1>
        <p className="mt-2 text-slate-600">You do not have access to this page.</p>
      </div>
    )
  }

  return <Outlet />
}
