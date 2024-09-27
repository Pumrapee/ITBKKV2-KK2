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
    sessionStorage.setItem("token", newToken)
    const tokenIsUser = sessionStorage.getItem("token")
    token.value = tokenIsUser
    user.value = jwtDecode(tokenIsUser)
    //username
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
    sessionStorage.clear()
  }

  const setToken = () => {
    token.value = sessionStorage.getItem("token")
  }

  // เพิ่มฟังก์ชัน getUserId
  const getUserId = () => {
    return user.value ? user.value.id : null // คืนค่า ID ของผู้ใช้หาก login แล้ว
  }

  return {
    isAuthenticated,
    user,
    login,
    logout,
    token,
    setToken,
    userName,
    getUserId, // เพิ่มการคืนค่า getUserId
  }
})
