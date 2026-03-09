import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(parseInt(localStorage.getItem('userId')) || null)
  const username = ref(localStorage.getItem('username') || '')
  const email = ref(localStorage.getItem('email') || '')
  const role = ref(localStorage.getItem('role') || '')
  const registerTime = ref(localStorage.getItem('registerTime') || '')
  const phone = ref(localStorage.getItem('phone') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')

  const setUser = (data) => {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    email.value = data.email
    role.value = data.role
    registerTime.value = data.registerTime
    phone.value = data.phone || ''
    avatar.value = data.avatar || ''
    
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('username', data.username)
    localStorage.setItem('email', data.email)
    localStorage.setItem('role', data.role)
    localStorage.setItem('registerTime', data.registerTime)
    localStorage.setItem('phone', data.phone || '')
    localStorage.setItem('avatar', data.avatar || '')
  }

  const updateProfile = (data) => {
    username.value = data.username
    email.value = data.email
    phone.value = data.phone || ''
    avatar.value = data.avatar || ''
    
    localStorage.setItem('username', data.username)
    localStorage.setItem('email', data.email)
    localStorage.setItem('phone', data.phone || '')
    localStorage.setItem('avatar', data.avatar || '')
  }

  const logout = () => {
    token.value = ''
    userId.value = null
    username.value = ''
    email.value = ''
    role.value = ''
    registerTime.value = ''
    phone.value = ''
    avatar.value = ''
    
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
    localStorage.removeItem('role')
    localStorage.removeItem('registerTime')
    localStorage.removeItem('phone')
    localStorage.removeItem('avatar')
  }

  const isLoggedIn = () => {
    return !!token.value
  }

  return {
    token,
    userId,
    username,
    email,
    role,
    registerTime,
    phone,
    avatar,
    setUser,
    updateProfile,
    logout,
    isLoggedIn
  }
})
