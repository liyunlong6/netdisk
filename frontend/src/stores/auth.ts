import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type User } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  
  const isLoggedIn = computed(() => !!token.value)
  
  // 初始化 - 从本地存储恢复
  function init() {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    
    if (savedToken) {
      token.value = savedToken
    }
    if (savedUser) {
      user.value = JSON.parse(savedUser)
    }
  }
  
  // 登录
  async function login(username: string, password: string) {
    const response = await authApi.login({ username, password })
    const data = response.data
    
    token.value = data.token
    user.value = data.user
    
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
  }
  
  // 注册
  async function register(username: string, email: string, password: string) {
    await authApi.register({ username, email, password })
  }
  
  // 登出
  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 即使失败也清除本地状态
    }
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  // 获取当前用户
  async function fetchCurrentUser() {
    try {
      const response = await authApi.getCurrentUser()
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(response.data))
    } catch {
      // 忽略错误
    }
  }
  
  // 更新存储使用量
  function updateStorageUsed(used: number) {
    if (user.value) {
      user.value.storageUsed = used
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }
  
  return {
    user,
    token,
    isLoggedIn,
    init,
    login,
    register,
    logout,
    fetchCurrentUser,
    updateStorageUsed
  }
})
