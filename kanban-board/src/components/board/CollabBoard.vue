<script setup>
import { onMounted, ref } from "vue"
import { useBoardStore } from "@/stores/boardStore"
import { useAuthStore } from "@/stores/loginStore"
import { useRoute } from "vue-router"
import {
  getItems,
  checkAndRefreshToken,
  addItem,
  getItemById,
} from "@/libs/fetchUtils"
import ExpireToken from "../toast/ExpireToken.vue"
import AddCollabBoard from "./AddCollabBoard.vue"
import Alert from "../toast/Alert.vue"
import { useRouter } from "vue-router"

const route = useRoute()
const router = useRouter()
const boardId = ref(route.params.id)
const myBoard = useBoardStore()
const myUser = useAuthStore()
const boardName = ref()
const expiredToken = ref(false)
const openModal = ref(false)
const modalAlert = ref({ message: "", type: "", modal: false })
const collab = ref()
const refreshToken = ref(localStorage.getItem("refreshToken"))

onMounted(async () => {
  myUser.setToken()
  expiredToken.value = false

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    const collabList = await getItems(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs`
    )

    //Collab
    if (collabList === 401) {
      expiredToken.value = true
    } else {
      if (myBoard.getCollabs().length === 0) {
        collabList.sort((a, b) => new Date(a.addedOn) - new Date(b.addedOn))

        myBoard.addCollabs(collabList)
      }
    }

    //Board
    const boardIdNumber = await getItemById(
      `${import.meta.env.VITE_API_URL}v3/boards`,
      boardId.value
    )

    boardName.value = boardIdNumber.name
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
})
console.log(myBoard.getCollabs())

const openModalAdd = () => {
  openModal.value = true
  collab.value = {
    email: "",
    access_right: "READ",
  }
}

const closeAddCollab = async (newCollab) => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    const { newTask, statusCode } = await addItem(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs`,
      newCollab.value
    )

    if (statusCode === 201) {
      myBoard.clearCollaborator()
      const collabList = await getItems(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs`
      )
      collabList.sort((a, b) => new Date(a.addedOn) - new Date(b.addedOn))
      myBoard.addCollabs(collabList)
      openModal.value = false
      showAlert("The collaborator has been successfully added.", "success")
    } else if (statusCode === 401) {
      openModal.value = false
      expiredToken.value = true
    } else if (statusCode === 403) {
      showAlert(
        "You do not have permission to add board collaborator.",
        "error"
      )
      openModal.value = false
    } else if (statusCode === 404) {
      showAlert("The user does not exist.", "error")
    } else if (statusCode === 409) {
      showAlert("The user is already the collaborator of this board.", "error")
    } else {
      showAlert("There is a problem. Please try again later.", "error")
    }
    collab.value = {
      email: "",
      access_right: "READ",
    }
    console.log(boardId.value)
    console.log(newCollab.value)
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
}

const closeModal = () => {
  openModal.value = false
}

//Alert
const showAlert = (message, type) => {
  modalAlert.value = {
    message,
    type,
    modal: true,
  }
  setTimeout(() => {
    modalAlert.value.modal = false
  }, 4000)
}
</script>

<template>
  <div class="bounce-in-top flex flex-col items-center mt-28 ml-60">
    <div
      class="font-bold text-4xl text-black self-center pb-5 flex items-center justify-between ml-20"
    >
      Collaborator Management
    </div>

    <!-- Navigation -->
    <div class="bounce-in-top flex justify-between w-4/5">
      <div class="flex text-sm breadcrumbs text-black">
        <ul>
          <li class="itbkk-board-name font-bold text-base">
            <RouterLink :to="{ name: 'task' }">{{ boardName }}</RouterLink>
          </li>
          <li>Collaborator</li>
        </ul>
      </div>
      <div class="flex items-center">
        <!-- <RouterLink :to="{ name: 'AddStatus' }"> -->
        <button
          @click="openModalAdd"
          class="itbkk-collaborator-add btn btn-circle border-black0 bg-black text-white ml-2"
        >
          <img src="/icons/plus.png" class="w-4" />
        </button>
        <!-- </RouterLink> -->
      </div>
    </div>

    <!-- Table -->
    <div class="border border-black rounded-md w-4/5 mt-4">
      <table class="table">
        <!-- head -->
        <thead class="bg-black">
          <tr class="text-white text-sm">
            <th class="pl-20">No.</th>
            <th class="pl-20">Name</th>
            <th class="pl-40">Email</th>
            <th class="pl-36">Access Right</th>
            <th class="pl-20">Action</th>
          </tr>
        </thead>

        <tbody v-if="myBoard.getCollabs().length > 0">
          <!-- row -->
          <tr
            v-for="(collab, index) in myBoard.getCollabs()"
            :key="collab.oid"
            class="itbkk-item"
          >
            <th class="text-black pl-20">{{ index + 1 }}</th>
            <td class="itbkk-name pl-10">{{ collab.name }}</td>
            <td class="itbkk-email pl-20">{{ collab.email }}</td>
            <td class="itbkk-access-right pl-40">
              <p
                class="shadow-md rounded-full h-auto max-w-40 font-medium text-center p-3 break-all bg-white"
              >
                {{ collab.accessRight }}
              </p>
            </td>

            <td class="itbkk-collab-remove"></td>
          </tr>
        </tbody>

        <tbody v-else>
          <tr>
            <td colspan="5" class="text-center py-4 text-gray-500 font-medium">
              No board collaborator
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <AddCollabBoard
    :showModal="openModal"
    :collabs="collab"
    @closeModal="closeModal"
    @addCollab="closeAddCollab"
  />

  <Alert
    :message="modalAlert.message"
    :type="modalAlert.type"
    :showAlert="modalAlert.modal"
  />

  <ExpireToken v-if="expiredToken" />
</template>

<style scoped></style>
