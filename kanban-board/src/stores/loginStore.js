import { defineStore } from "pinia"
import { ref } from "vue"
import { jwtDecode } from "jwt-decode"
import { useBoardStore } from "@/stores/boardStore"
import { useTaskStore } from "@/stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"
import { useLimitStore } from "./limitStore"
import router from "@/router"

export const useAuthStore = defineStore("auth", () => {
  const isAuthenticated = ref(false)
  const user = ref(null)
  const token = ref("")
  const userName = ref("")
  const myBoard = useBoardStore()
  const myStatus = useStatusStore()
  const myTask = useTaskStore()
  const myLimit = useLimitStore()

  const login = (newToken) => {
    isAuthenticated.value = true
    //token
    localStorage.setItem("token", newToken.access_token)
    localStorage.setItem("refreshToken", newToken.refresh_token)
    const tokenIsUser = localStorage.getItem("token")
    token.value = tokenIsUser
    user.value = jwtDecode(tokenIsUser)
    userName.value = user.value.name
    //username
    localStorage.setItem("user", user.value.name)
  }

  const logout = () => {
    isAuthenticated.value = false
    user.value = null
    userName.value = null
    token.value = ""
    myBoard.clearBoard()
    myStatus.clearStatus()
    myTask.clearTask()
    myLimit.clearLimit()
    myBoard.navBoard = false
    localStorage.clear()
    router.push({ name: "login" })
  }

  const setToken = () => {
    token.value = localStorage.getItem("token")
  }

  const accessDenied = () => {
    myBoard.clearBoard()
    myStatus.clearStatus()
    myTask.clearTask()
    myLimit.clearLimit()
  }

  // เพิ่มฟังก์ชัน getUserId
  const getUserId = () => {
    return user.value ? user.value.id : null // คืนค่า ID ของผู้ใช้หาก login แล้ว
  }

  const setNewToken = (newRefreshToken) => {
    token.value = newRefreshToken
    localStorage.setItem("token", newRefreshToken)
  }

  return {
    isAuthenticated,
    user,
    login,
    logout,
    token,
    setToken,
    setNewToken,
    userName,
    accessDenied,
    getUserId, // เพิ่มการคืนค่า getUserId
  }
})
