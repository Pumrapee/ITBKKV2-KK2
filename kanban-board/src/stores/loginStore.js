import { defineStore } from 'pinia'
import { ref } from 'vue'
import {jwtDecode} from 'jwt-decode'

export const useAuthStore = defineStore('auth', () => {
  const isAuthenticated = ref(false)
  const token = ref(null)
  const user = ref(null)

  const login = (jwtToken) => {
    token.value = jwtToken
    isAuthenticated.value = true
    user.value = decodeToken(jwtToken)
    console.log(user.value)
  }

  const logout = () => {
    token.value = null
    isAuthenticated.value = false
    user.value = null
  }

  const decodeToken = (jwtToken) => {
    try {
      return jwtDecode(jwtToken)
    } catch (error) {
      console.error('Invalid token:', error)
      return null
    }
  }

  return {
    isAuthenticated,
    token,
    user,
    login,
    logout
  }
})
