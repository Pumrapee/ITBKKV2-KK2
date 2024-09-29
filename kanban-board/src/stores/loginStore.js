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
    sessionStorage.setItem("token", newToken.access_token)
    sessionStorage.setItem("refreshToken", newToken.refresh_token)
    const tokenIsUser = sessionStorage.getItem("token")
    token.value = tokenIsUser
    user.value = jwtDecode(tokenIsUser)
    userName.value = user.value.name 
    //username
    sessionStorage.setItem("user", user.value.name)
  }

  const logout = () => {
    isAuthenticated.value = false
    user.value = null
    userName.value = null
    token.value = ""
    myBoard.clearBoard()
    myStatus.clearStatus()
    myTask.clearTask()
    myBoard.navBoard = false
    sessionStorage.clear()
    router.push({ name: "login" })
  }

  const setToken = () => {
    token.value = sessionStorage.getItem("token")
  }


  // เพิ่มฟังก์ชัน getUserId
  const getUserId = () => {
    return user.value ? user.value.id : null // คืนค่า ID ของผู้ใช้หาก login แล้ว
  }

  const setNewToken = (newRefreshToken) => {
    token.value = newRefreshToken
    sessionStorage.setItem("token", newRefreshToken)
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
    getUserId, // เพิ่มการคืนค่า getUserId
  }
})
