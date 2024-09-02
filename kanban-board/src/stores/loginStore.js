import { defineStore } from "pinia"
import { ref } from "vue"
import { jwtDecode } from "jwt-decode"

export const useAuthStore = defineStore("auth", () => {
  const isAuthenticated = ref(false)
  const user = ref(null)
  const token = ref("")

  const login = (newToken) => {
    isAuthenticated.value = true
    //token
    localStorage.setItem("token", newToken)
    const tokenIsUser = localStorage.getItem("token")
    user.value = jwtDecode(tokenIsUser)

    //username
    localStorage.setItem("user", user.value.name)
  }

  const logout = () => {
    isAuthenticated.value = false
    user.value = null
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
