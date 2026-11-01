import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'

export function NotFoundPage() {
  return (
    <div className="mx-auto max-w-lg py-16 text-center">
      <h1 className="text-3xl font-semibold">404</h1>
      <p className="mt-2 text-slate-600">Page not found.</p>
      <div className="mt-6">
        <Link to="/">
          <Button>Go home</Button>
        </Link>
      </div>
    </div>
  )
}
