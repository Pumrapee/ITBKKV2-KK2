import { defineStore } from "pinia"
import { ref } from "vue"
import { jwtDecode } from "jwt-decode"

export const useAuthStore = defineStore("auth", () => {
  const isAuthenticated = ref(false)
  const user = ref(null)
  const token = ref("")

  const setToken = (tokens) => {
    token.value = tokens
    localStorage.setItem("token", tokens)
  }

  const login = (newToken) => {
    isAuthenticated.value = true
    localStorage.setItem("token", newToken)
    user.value = jwtDecode(newToken)
  }

  const logout = () => {
    isAuthenticated.value = false
    user.value = null
    token.value = ""
    localStorage.clear()
  }

  return {
    isAuthenticated,
    user,
    login,
    logout,
    token,
  }
})
