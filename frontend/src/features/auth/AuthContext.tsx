import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { login as loginRequest, register as registerRequest } from '@/api/endpoints/auth'
import { getStoredToken, setStoredToken } from '@/api/client'
import type { AuthResponse, AuthUser } from '@/api/types'

type AuthContextValue = {
  user: AuthUser | null
  token: string | null
  isLoading: boolean
  isAdmin: boolean
  login: (email: string, password: string) => Promise<AuthUser>
  register: (payload: {
    email: string
    password: string
    firstName: string
    lastName: string
  }) => Promise<AuthUser>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)
const USER_KEY = 'ecom_auth_user'

function toUser(response: AuthResponse): AuthUser {
  return {
    id: response.userId,
    email: response.email,
    firstName: response.firstName,
    lastName: response.lastName,
    role: response.role,
  }
}

function readStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

function readInitialSession(): { token: string | null; user: AuthUser | null } {
  const storedToken = getStoredToken()
  const storedUser = readStoredUser()
  if (storedToken && storedUser) {
    return { token: storedToken, user: storedUser }
  }
  setStoredToken(null)
  localStorage.removeItem(USER_KEY)
  return { token: null, user: null }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const initial = readInitialSession()
  const [token, setToken] = useState<string | null>(initial.token)
  const [user, setUser] = useState<AuthUser | null>(initial.user)

  const persistSession = useCallback((response: AuthResponse) => {
    const nextUser = toUser(response)
    setStoredToken(response.accessToken)
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
    setToken(response.accessToken)
    setUser(nextUser)
    return nextUser
  }, [])

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await loginRequest({ email, password })
      return persistSession(response)
    },
    [persistSession],
  )

  const register = useCallback(
    async (payload: { email: string; password: string; firstName: string; lastName: string }) => {
      const response = await registerRequest(payload)
      return persistSession(response)
    },
    [persistSession],
  )

  const logout = useCallback(() => {
    setStoredToken(null)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      isLoading: false,
      isAdmin: user?.role === 'ADMIN',
      login,
      register,
      logout,
    }),
    [user, token, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components -- hook colocated with provider
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
