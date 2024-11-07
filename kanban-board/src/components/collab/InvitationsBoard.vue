<script setup>
import { ref, onMounted } from "vue"
import {
  getBoardItems,
  invitation,
  checkAndRefreshToken,
} from "@/libs/fetchUtils"
import { useRoute } from "vue-router"
import { useBoardStore } from "@/stores/boardStore.js"
import { useAuthStore } from "@/stores/loginStore"
import router from "@/router"
import ExpireToken from "../toast/ExpireToken.vue"

//store
const myBoard = useBoardStore()
const myUser = useAuthStore()

const route = useRoute()
const boardId = ref(route.params.id)
const inviter = ref({})
const resultInvitation = ref({ invitation: "" })
const matchCollab = ref(false)
const loading = ref(true) // เพิ่มตัวแปร loading
const refreshToken = ref(localStorage.getItem("refreshToken"))
const expiredToken = ref(false)

onMounted(async () => {
  myBoard.clearBoardCollab()
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    myUser.setNewToken(checkToken.accessNewToken)

    const listBoard = await getBoardItems(
      `${import.meta.env.VITE_API_URL}v3/boards`
    )

    const collab = listBoard.collab.find((boards) => {
      return boards.id === boardId.value
    })

    if (collab && collab.status === "PENDING") {
      inviter.value = {
        name: collab?.owner.name,
        accessRight: collab?.accessRight,
        boardName: collab?.name,
      }
      matchCollab.value = true
    } else {
      matchCollab.value = false
      setTimeout(() => {
        router.go(-1)
      }, 3000)
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }

  loading.value = false // เสร็จสิ้นการโหลดข้อมูล
})

const acceptInvitation = async () => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    myUser.setNewToken(checkToken.accessNewToken)

    resultInvitation.value.invitation = "ACCEPT"
    const statusCode = await invitation(
      `${import.meta.env.VITE_API_URL}v3/boards/${
        boardId.value
      }/collabs/invitations`,
      resultInvitation.value
    )

    if (statusCode === 200) {
      router.push({ name: "task", params: { id: boardId.value } })
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
}

const declineInvitation = async () => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    myUser.setNewToken(checkToken.accessNewToken)

    resultInvitation.value.invitation = "DECLINE"
    const statusCode = await invitation(
      `${import.meta.env.VITE_API_URL}v3/boards/${
        boardId.value
      }/collabs/invitations`,
      resultInvitation.value
    )

    if (statusCode === 200) {
      router.go(-1)
      myBoard.clearBoardCollab()
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
}
</script>

<template>
  <div class="bounce-in-top flex flex-col items-center mt-28 ml-60">
    <div class="flex items-center justify-center">
      <!-- Loading State -->
      <div
        v-if="loading"
        class="bg-white p-8 rounded-lg shadow-lg max-w-md text-center mt-32"
      >
        <span class="loading loading-spinner loading-lg"></span>
        <p class="text-gray-700">
          Please wait while we retrieve your invitation.
        </p>
      </div>

      <!-- Invitation -->
      <div
        v-if="!loading && matchCollab"
        class="bg-white p-8 rounded-lg shadow-lg max-w-md text-center mt-32"
      >
        <div class="flex justify-center mb-4">
          <img src="/icons/invitation.png" class="w-20" />
        </div>
        <p class="text-gray-700 mb-6">
          <span class="font-bold">{{ inviter.name }}</span> has invited you to
          collaborate with
          <span class="font-bold">{{ inviter.accessRight }}</span> access right
          on <span class="font-bold">{{ inviter.boardName }}</span> board
        </p>
        <div class="flex justify-center space-x-4">
          <button
            class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 focus:outline-none"
            @click="acceptInvitation"
          >
            Accept
          </button>
          <button
            class="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 focus:outline-none"
            @click="declineInvitation"
          >
            Decline
          </button>
        </div>
      </div>

      <!--Non Invitation -->
      <div
        v-if="!loading && !matchCollab"
        class="bg-white p-8 rounded-lg shadow-lg max-w-md text-center mt-32"
      >
        <div class="flex justify-center mb-4">
          <img src="/icons/invitation.png" class="w-20" />
        </div>
        <p class="text-gray-700 mb-6">
          Sorry, we couldn't find your active invitation to this board.
        </p>
      </div>
    </div>
  </div>

  <ExpireToken v-if="expiredToken" />
</template>

<style scoped></style>
