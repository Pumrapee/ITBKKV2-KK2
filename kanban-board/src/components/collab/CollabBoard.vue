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
  patchItem,
  deleteItemById,
} from "@/libs/fetchUtils"
import ExpireToken from "../toast/ExpireToken.vue"
import AddCollabBoard from "../collab/AddCollabBoard.vue"
import { showAlert } from "../../libs/alertUtils"
import RemoveCollaborator from "../collab/RemoveCollaborator.vue"

//store
const myBoard = useBoardStore()
const myUser = useAuthStore()

const route = useRoute()
const boardId = ref(route.params.id)
const boardName = ref()
const expiredToken = ref(false)
const openModal = ref(false)
const collab = ref()
const refreshToken = ref(localStorage.getItem("refreshToken"))
const showDeleteModal = ref(false)
const showCancelModal = ref(false)
const collaboratorToRemove = ref({ id: "", name: "" })
const nameOwnerBoard = ref()
const disabledIfNotOwner = ref()

const showAccessModal = ref(false)
const selectedCollab = ref(null) // เก็บ collaborator ที่เลือก
const newAccessRight = ref("") // เก็บสิทธิ์ใหม่
const selectedCollabName = ref("")

onMounted(async () => {
  myUser.setToken()
  expiredToken.value = false

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    myUser.setNewToken(checkToken.accessNewToken)

    if (myBoard.getCollabs().length === 0) {
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
    }

    //Board
    const boardIdNumber = await getItemById(
      `${import.meta.env.VITE_API_URL}v3/boards`,
      boardId.value
    )

    boardName.value = boardIdNumber.name
    nameOwnerBoard.value = boardIdNumber.owner.name
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }

  if (nameOwnerBoard.value !== localStorage.getItem("user")) {
    disabledIfNotOwner.value = true
  }
})

const openModalAdd = () => {
  openModal.value = true
  collab.value = {
    email: "",
    accessRight: "READ",
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
    myUser.setNewToken(checkToken.accessNewToken)

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
      showAlert(
        "The user is already the collaborator or pending collaborator of this board.",
        "error"
      )
    } else {
      showAlert("There is a problem. Please try again later.", "error")
    }
    collab.value = {
      email: "",
      accessRight: "READ",
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    openModal.value = false
  }
}

const closeModal = () => {
  openModal.value = false
  showCancelModal.value = false
  showDeleteModal.value = false
}

const openDeleteModal = (id, name) => {
  collaboratorToRemove.value = { id, name }
  showDeleteModal.value = true
}

const openCancelModal = (id, name) => {
  collaboratorToRemove.value = { id, name }
  showCancelModal.value = true
}

const confirmRemoveCollaborator = async () => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    myUser.setNewToken(checkToken.accessNewToken)

    const statusCode = await deleteItemById(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs`,
      collaboratorToRemove.value.id
    )

    if (statusCode === 200) {
      showAlert("Collaborator removed successfully.", "success")
      myBoard.removeCollab(collaboratorToRemove.value.id) // ลบ collaborator ออกจาก list
      showDeleteModal.value = false // ปิด modal
      showCancelModal.value = false
    } else if (statusCode === 403) {
      showAlert(
        "You do not have permission to add board collaborator.",
        "error"
      )
    } else if (statusCode === 404) {
      myBoard.removeCollab(collaboratorToRemove.value.id)
      showDeleteModal.value = false
      showCancelModal.value = false
      showAlert(
        `${collaboratorToRemove.value.name} is not a collaborator.`,
        "error"
      )
    } else if (statusCode === 401) {
      expiredToken.value = true
      showDeleteModal.value = false
      showCancelModal.value = false
    } else {
      // 500
      showAlert("There is a problem. Please try again later.", "error")
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    showDeleteModal.value = false
    showCancelModal.value = false
  }
}

const changeAccessRight = async (collab, newRight) => {
  collab.accessRight = newRight
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    myUser.setNewToken(checkToken.accessNewToken)

    const { statusCode } = await patchItem(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs/${
        collab.oid
      }`,
      { accessRight: newRight }
    )

    if (statusCode === 200) {
      showAlert("Access right updated successfully.", "success")
    } else if (statusCode === 403) {
      showAlert(
        "You do not have permission to add board collaborator.",
        "error"
      )
    } else {
      showAlert("There is a problem. Please try again later.", "error")
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
}

const openAccessModal = (collab, accessRight) => {
  if (collab.accessRight === accessRight) {
    return // ถ้าค่าเท่ากัน จะไม่ทำการเปิด Modal
  }

  selectedCollab.value = collab
  newAccessRight.value = accessRight
  showAccessModal.value = true
  selectedCollabName.value = collab.name
}

const confirmAccessRightChange = async () => {
  await changeAccessRight(selectedCollab.value, newAccessRight.value)
  showAccessModal.value = false
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
          :disabled="disabledIfNotOwner"
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
            <th class="pl-32">Email</th>
            <th class="pl-16">Access Right</th>
            <th class="pl-20 pr-10">Action</th>
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
            <td class="itbkk-name pl-10">
              {{ collab.name }}
              <p v-if="collab.status === `PENDING`" class="text-slate-400">
                (Pending Invite)
              </p>
            </td>
            <td class="itbkk-email pl-10">{{ collab.email }}</td>
            <td class="itbkk-access-right pl-10">
              <div class="dropdown">
                <label
                  tabindex="0"
                  class="btn btn-ghost shadow-md rounded-full h-auto w-28 font-medium text-center p-3 break-all bg-white"
                  :disabled="disabledIfNotOwner"
                >
                  {{ collab.accessRight }}
                  <svg
                    class="w-6 h-6 text-gray-800 dark:text-white"
                    aria-hidden="true"
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke="currentColor"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="m8 10 4 4 4-4"
                    />
                  </svg>
                </label>
                <ul
                  tabindex="0"
                  class="dropdown-content menu p-2 shadow bg-base-100 rounded-box w-52 z-[1]"
                >
                  <li>
                    <a @click="openAccessModal(collab, 'READ')">READ</a>
                  </li>
                  <li>
                    <a @click="openAccessModal(collab, 'WRITE')">WRITE</a>
                  </li>
                </ul>
              </div>
            </td>

            <td v-if="collab.status === `MEMBER`" class="itbkk-collab-remove">
              <div class="ml-16 relative group inline-block">
                <button
                  :disabled="disabledIfNotOwner"
                  class="itbkk-button-delete btn bg-red-500"
                  @click="openDeleteModal(collab.oid, collab.name)"
                >
                  <img src="/icons/delete.png" class="w-4" />
                </button>
              </div>
            </td>

            <td v-if="collab.status === `PENDING`" class="itbkk-collab-remove">
              <div class="ml-16 relative group inline-block">
                <button
                  :disabled="disabledIfNotOwner"
                  class="btn"
                  @click="openCancelModal(collab.oid, collab.name)"
                >
                  Cancel
                </button>
              </div>
            </td>
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

  <!-- Modal for changing access right -->
  <div
    v-if="showAccessModal"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="bg-white p-6 rounded-lg shadow-lg max-w-md w-full">
      <h2 class="text-xl font-semibold mb-4">Change Access Right</h2>
      <p class="mb-6">
        Do you want to change access right of
        <span class="font-bold text-blue-400">{{ selectedCollabName }}</span>
        to <span class="font-bold text-blue-400">{{ newAccessRight }}</span
        >?
      </p>
      <div class="flex justify-end space-x-4">
        <button
          @click="showAccessModal = false"
          class="px-4 py-2 bg-gray-300 text-white rounded-md hover:bg-gray-400"
        >
          Cancel
        </button>
        <button
          @click="confirmAccessRightChange"
          class="px-4 py-2 bg-black text-white rounded-md hover:bg-blue-600"
        >
          Confirm
        </button>
      </div>
    </div>
  </div>

  <AddCollabBoard
    :showModal="openModal"
    :collabs="collab"
    @closeModal="closeModal"
    @addCollab="closeAddCollab"
  />

  <RemoveCollaborator
    :showDelete="showDeleteModal"
    :showCancel="showCancelModal"
    :selectedCollabName="collaboratorToRemove.name"
    @confirmRemove="confirmRemoveCollaborator"
    @cancelDelete="closeModal"
  />

  <ExpireToken v-if="expiredToken" />
</template>

<style scoped></style>
