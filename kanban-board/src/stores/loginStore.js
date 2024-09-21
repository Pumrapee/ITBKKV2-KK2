import { defineStore } from "pinia"
import { ref } from "vue"
import { jwtDecode } from "jwt-decode"
import { useBoardStore } from "@/stores/boardStore"
import { useTaskStore } from "@/stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"
import router from "@/router"

export const useAuthStore = defineStore("auth", () => {
  const isAuthenticated = ref(false)
  const user = ref(null)
  const token = ref("")
  const userName = ref("")
  const myBoard = useBoardStore()
  const myStatus = useStatusStore()
  const myTask = useTaskStore()

  const login = (newToken) => {
    isAuthenticated.value = true
    //token
    localStorage.setItem("token", newToken)
    const tokenIsUser = localStorage.getItem("token")
    token.value = tokenIsUser
    user.value = jwtDecode(tokenIsUser)
    //username
    // userName.value = user.value.name
    // localStorage.setItem("user", user.value.name)
    sessionStorage.setItem("user", user.value.name)

  }

  const logout = () => {
    isAuthenticated.value = false
    user.value = null
    userName.value = null
    myBoard.clearBoard()
    myStatus.clearStatus()
    myTask.clearTask()
    myBoard.navBoard = false
    router.push({ name: "login" })
    localStorage.clear()
  }

  const setToken = () => {
    token.value = localStorage.getItem("token")
  }

  return {
    isAuthenticated,
    user,
    login,
    logout,
    token,
    setToken,
    userName,
  }
})
