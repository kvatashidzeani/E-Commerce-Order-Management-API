import { useState } from 'react'
import { categoryTheme } from '@/features/products/categoryTheme'
import { cn } from '@/lib/utils'

type ProductImageProps = {
  src?: string | null
  alt: string
  categoryName?: string | null
  className?: string
  imgClassName?: string
}

export function ProductImage({
  src,
  alt,
  categoryName,
  className,
  imgClassName,
}: ProductImageProps) {
  const [failed, setFailed] = useState(false)
  const theme = categoryTheme(categoryName)
  const showImage = Boolean(src) && !failed

  return (
    <div
      className={cn('relative overflow-hidden', className)}
      style={
        showImage
          ? undefined
          : { background: `linear-gradient(145deg, ${theme.accent}, ${theme.ink})` }
      }
    >
      {showImage ? (
        <img
          src={src!}
          alt={alt}
          loading="lazy"
          className={cn('h-full w-full object-cover transition duration-500 group-hover:scale-105', imgClassName)}
          onError={() => setFailed(true)}
        />
      ) : (
        <div className="flex h-full w-full items-center justify-center">
          <span className="font-display text-4xl font-bold text-white/30">
            {(categoryName ?? alt).slice(0, 1).toUpperCase()}
          </span>
        </div>
      )}
    </div>
  )
}
