export type ProblemDetail = {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  path?: string
  errors?: Record<string, string>
}

export class ApiError extends Error {
  readonly status: number
  readonly title?: string
  readonly fieldErrors: Record<string, string>

  constructor(problem: ProblemDetail, fallbackMessage = 'Request failed') {
    super(problem.detail ?? problem.title ?? fallbackMessage)
    this.name = 'ApiError'
    this.status = problem.status ?? 500
    this.title = problem.title
    this.fieldErrors = problem.errors ?? {}
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}
