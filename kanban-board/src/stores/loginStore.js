import { defineStore } from "pinia"
import { ref } from "vue"
import { jwtDecode } from "jwt-decode"

export const useAuthStore = defineStore("auth", () => {
  const isAuthenticated = ref(false)
  const user = ref(null)
  const token = ref("")

  const setToken = (tokens) => {
    token.value = tokens
  }

  const login = () => {
    isAuthenticated.value = true
    user.value = jwtDecode(token.value)
  }

  const logout = () => {
    isAuthenticated.value = false
    user.value = null
  }

  return {
    isAuthenticated,
    user,
    login,
    logout,
    setToken,
    token,
  }
})
