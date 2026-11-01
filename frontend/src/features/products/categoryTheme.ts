export type CategoryTheme = {
  accent: string
  soft: string
  ink: string
  pattern: string
}

const FALLBACK: CategoryTheme = {
  accent: '#0f766e',
  soft: '#ccfbf1',
  ink: '#134e4a',
  pattern: 'radial-gradient(circle at 20% 20%, rgba(255,255,255,.35), transparent 45%)',
}

const THEMES: Record<string, CategoryTheme> = {
  electronics: {
    accent: '#0e7490',
    soft: '#cffafe',
    ink: '#164e63',
    pattern: 'linear-gradient(135deg, rgba(255,255,255,.28), transparent 50%)',
  },
  laptops: {
    accent: '#1d4ed8',
    soft: '#dbeafe',
    ink: '#1e3a8a',
    pattern: 'radial-gradient(circle at 80% 10%, rgba(255,255,255,.4), transparent 40%)',
  },
  phones: {
    accent: '#0f766e',
    soft: '#ccfbf1',
    ink: '#115e59',
    pattern: 'linear-gradient(160deg, rgba(255,255,255,.3), transparent 55%)',
  },
  books: {
    accent: '#b45309',
    soft: '#fef3c7',
    ink: '#78350f',
    pattern: 'repeating-linear-gradient(90deg, rgba(255,255,255,.12) 0 2px, transparent 2px 14px)',
  },
  clothing: {
    accent: '#be123c',
    soft: '#ffe4e6',
    ink: '#9f1239',
    pattern: 'radial-gradient(circle at 30% 70%, rgba(255,255,255,.35), transparent 50%)',
  },
  food: {
    accent: '#c2410c',
    soft: '#ffedd5',
    ink: '#9a3412',
    pattern: 'linear-gradient(120deg, rgba(255,255,255,.25), transparent 60%)',
  },
  home: {
    accent: '#047857',
    soft: '#d1fae5',
    ink: '#065f46',
    pattern: 'radial-gradient(circle at 70% 30%, rgba(255,255,255,.35), transparent 45%)',
  },
  sports: {
    accent: '#0369a1',
    soft: '#e0f2fe',
    ink: '#0c4a6e',
    pattern: 'linear-gradient(45deg, rgba(255,255,255,.2) 25%, transparent 25%)',
  },
}

export function categoryTheme(name?: string | null): CategoryTheme {
  if (!name) return FALLBACK
  return THEMES[name.trim().toLowerCase()] ?? FALLBACK
}

export function categoryInitials(name: string): string {
  return name
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')
}
