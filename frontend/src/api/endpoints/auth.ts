import { apiClient } from '@/api/client'
import type { AuthResponse } from '@/api/types'

export type RegisterPayload = {
  email: string
  password: string
  firstName: string
  lastName: string
}

export type LoginPayload = {
  email: string
  password: string
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/auth/register', payload)
  return data
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/api/auth/login', payload)
  return data
}
