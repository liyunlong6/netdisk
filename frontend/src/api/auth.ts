import api from './index'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface User {
  id: number
  username: string
  email: string
  nickname?: string
  storageUsed: number
  storageQuota: number
}

export interface AuthResponse {
  token: string
  refreshToken: string
  user: User
}

export const authApi = {
  login: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
  
  register: (data: RegisterRequest) => api.post('/auth/register', data),
  
  getCurrentUser: () => api.get<User>('/auth/me'),
  
  logout: () => api.post('/auth/logout')
}
