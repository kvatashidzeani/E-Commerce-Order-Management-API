import axios, { type AxiosError } from 'axios'
import { ApiError, type ProblemDetail } from '@/api/errors'

const TOKEN_KEY = 'ecom_access_token'

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8081',
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ProblemDetail>) => {
    if (error.response?.status === 401) {
      setStoredToken(null)
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.assign('/login')
      }
    }

    const problem = error.response?.data
    if (problem && typeof problem === 'object') {
      return Promise.reject(new ApiError(problem))
    }

    return Promise.reject(
      new ApiError({
        status: error.response?.status,
        detail: error.message || 'Network error',
        title: 'Request Failed',
      }),
    )
  },
)
