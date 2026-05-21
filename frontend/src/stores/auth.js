import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => user.value?.role || '')

  async function login(username, password) {
    const response = await api.post('/auth/login', { username, password })
    const data = response.data
    token.value = data.token
    user.value = { username: data.username, fullName: data.fullName, role: data.role }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
    return data
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function hasRole(...roles) {
    return roles.includes(userRole.value)
  }

  return { user, token, isAuthenticated, userRole, login, logout, hasRole }
})
