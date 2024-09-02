import { defineStore } from "pinia"
import { ref } from "vue"
import { jwtDecode } from "jwt-decode"
import { useBoardStore } from "@/stores/boardStore.js"
import { getItems } from "@/libs/fetchUtils"

export const useAuthStore = defineStore("auth", () => {
  const isAuthenticated = ref(false)
  const user = ref(null)
  const token = ref("")
  const useBoard = useBoardStore()

  const setToken = (tokens) => {
    token.value = tokens
    localStorage.setItem("token", tokens)
  }

  const login = (newToken) => {
    isAuthenticated.value = true
    localStorage.setItem("token", newToken)
    user.value = jwtDecode(newToken)

    // setTimeout(async () => {
    //   const listBoard = await getItems(`${import.meta.env.VITE_API_URL}boards`)
    //   console.log(listBoard)
    //   useBoard.addBoards(listBoard)
    // }, 5000)
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
    setToken,
  }
})
