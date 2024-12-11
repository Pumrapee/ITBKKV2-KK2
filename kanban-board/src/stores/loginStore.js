import { defineStore } from "pinia"
import { ref } from "vue"
import { jwtDecode } from "jwt-decode"
import { useBoardStore } from "@/stores/boardStore"
import { useTaskStore } from "@/stores/taskStore"
import { useStatusStore } from "@/stores/statusStore"
import { useLimitStore } from "./limitStore"
import router from "@/router"
import { msalInstance, loginRequest } from "@/configs/msalConfig"

export const useAuthStore = defineStore("auth", () => {
  const isAuthenticated = ref(false)
  const user = ref(null)
  const token = ref("")
  const userName = ref("")
  const myBoard = useBoardStore()
  const myStatus = useStatusStore()
  const myTask = useTaskStore()
  const myLimit = useLimitStore()
  const isMicrosoftLogin = ref(false)

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
    //oid
    localStorage.setItem("oid", user.value.oid)
  }

  const microsoftLogin = async () => {
    try {
      const loginResponse = await msalInstance.loginPopup(loginRequest) // Use popup or redirect as preferred
      const msToken = loginResponse.accessToken
      isAuthenticated.value = true
      isMicrosoftLogin.value = true // Mark as Microsoft login
      token.value = msToken
      user.value = msalInstance.getAccountByHomeId(
        loginResponse.account.homeAccountId
      )
      userName.value = user.value.name
      localStorage.setItem("token", msToken)
      localStorage.setItem("user", user.value.name)
      localStorage.setItem("oid", user.value.localAccountId)
      router.push({ name: "board" })
    } catch (error) {
      console.error("Microsoft Login Error:", error)
    }
  }

  const logout = async () => {
    try {
      isAuthenticated.value = false
      user.value = null
      userName.value = null
      token.value = ""
      myBoard.clearBoard()
      myBoard.clearBoardCollab()
      myBoard.clearCollaborator()
      myStatus.clearStatus()
      myTask.clearTask()
      myLimit.clearLimit()
      myBoard.navBoard = false
      localStorage.clear()
      router.push({ name: "login" })
    } catch (error) {
      console.error("Microsoft Logout Error:", error)
    }
  }

  const setToken = () => {
    token.value = localStorage.getItem("token")
  }

  const accessDenied = () => {
    myBoard.clearBoard()
    myBoard.clearBoardCollab()
    myBoard.clearCollaborator()
    myStatus.clearStatus()
    myTask.clearTask()
    myLimit.clearLimit()
  }

  const setNewToken = (newRefreshToken) => {
    token.value = newRefreshToken
    localStorage.setItem("token", newRefreshToken)
  }

  return {
    isAuthenticated,
    user,
    login,
    microsoftLogin,
    logout,
    token,
    setToken,
    setNewToken,
    userName,
    accessDenied,
  }
})
