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
const loadingEmail = ref(false)
const NotSendEmail = ref(false)
const collabIsNotSentEmail = ref()
const link = ref(import.meta.env.VITE_API_URL)

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
  openModal.value = false
  loadingEmail.value = true

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

    if (statusCode === 201 || statusCode === 503) {
      const collabList = await getItems(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs`
      )
      collabList.sort((a, b) => new Date(a.addedOn) - new Date(b.addedOn))

      loadingEmail.value = false
      myBoard.clearCollaborator()
      myBoard.addCollabs(collabList)

      if (statusCode === 201) {
        showAlert("The collaborator has been successfully added.", "success")
      } else if (statusCode === 503) {
        NotSendEmail.value = true
        showAlert("The collaborator has been successfully added.", "success")

        const collabName = collabList.find(
          (collab) => collab.email === newCollab.value.email
        )
        collabIsNotSentEmail.value = collabName
      }
    } else {
      setTimeout(() => {
        loadingEmail.value = false
        openModal.value = true

        if (statusCode === 401) {
          expiredToken.value = true
        } else if (statusCode === 403) {
          showAlert(
            "You do not have permission to add board collaborator.",
            "error"
          )
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
      }, 3000)
    }

    // รีเซ็ตค่า newCollab
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
        "You do not have permission to removed board collaborator.",
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
        "You do not have permission to change collaborator access right.",
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
  <div
    class="bounce-in-top flex flex-col items-center mt-28 lg:ml-36 lg:mt-32 md:mt-12 sm:mt-8 px-4 md:px-10"
  >
    <div
      class="font-bold text-4xl lg:text-3xl md:text-2xl sm:text-xl text-black self-center pb-5 flex items-center justify-center ml-8 lg:ml-12 md:ml-8 sm:ml-2"
    >
      Collaborator Management
    </div>

    <!-- Navigation -->
    <div
      class="bounce-in-top flex flex-col lg:flex-row justify-center lg:justify-between w-full lg:w-4/5 flex-wrap mb-4"
    >
      <div
        class="flex text-sm breadcrumbs text-black mb-4 md:mb-0 justify-center lg:justify-start"
      >
        <ul class="flex flex-wrap">
          <li class="itbkk-board-name font-bold text-base md:text-sm">
            <RouterLink :to="{ name: 'task' }">{{ boardName }}</RouterLink>
          </li>
          <li>Collaborator</li>
        </ul>
      </div>

      <div class="flex flex-row justify-center lg:justify-end">
        <RouterLink :to="{ name: 'task' }">
          <button
            class="itbkk-button-home btn ml-10 bg-black text-white text-sm md:text-base w-full md:w-auto mb-2 md:mb-0"
          >
            <img src="/icons/home.png" class="w-4 mr-1" /> Home
          </button>
        </RouterLink>

        <div
          class="justify-center w-full md:w-auto lg:justify-end relative group"
        >
          <button
            @click="openModalAdd"
            class="itbkk-collaborator-add btn btn-circle border-black0 bg-black text-white ml-12 md:ml-2"
            :disabled="disabledIfNotOwner"
          >
            <img src="/icons/plus.png" class="w-4" />
          </button>

          <!-- Tooltip -->
          <div
            v-if="disabledIfNotOwner"
            class="absolute bottom-full mb-2 hidden w-32 group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
          >
            You need to be board owner to perform this action.
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div
      class="border border-black rounded-md w-full lg:w-4/5 mt-4 overflow-x-auto lg:overflow-x-visible"
    >
      <table class="table w-full text-xs md:text-sm">
        <!-- head -->
        <thead class="bg-black">
          <tr class="text-white text-sm">
            <th class="pl-20 hidden md:table-cell">No.</th>
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
            <th class="text-black pl-20 hidden md:table-cell">
              {{ index + 1 }}
            </th>

            <td class="itbkk-name pl-10">
              {{ collab.name }}
              <p v-if="collab.status === `PENDING`" class="text-slate-400">
                (Pending Invite)
              </p>
            </td>
            <td class="itbkk-email pl-10">{{ collab.email }}</td>
            <td class="itbkk-access-right pl-10">
              <div class="dropdown relative group">
                <label
                  tabindex="0"
                  class="btn btn-ghost shadow-md rounded-full h-auto w-28 font-medium text-center p-2 break-words bg-white"
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
                <!-- Tooltip -->
                <div
                  v-if="disabledIfNotOwner"
                  class="absolute bottom-full mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
                >
                  You need to be board owner to perform this action.
                </div>

                <ul
                  tabindex="0"
                  class="dropdown-content menu p-2 shadow bg-base-100 rounded-box w-full sm:w-52 z-[1]"
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
                  class="itbkk-button-delete btn bg-red-500 rounded-full w-12 h-12 sm:w-10 sm:h-10 md:w-12 md:h-12"
                  @click="openDeleteModal(collab.oid, collab.name)"
                >
                  <img src="/icons/delete.png" class="w-4" />
                </button>
                <!-- Tooltip -->
                <div
                  v-if="disabledIfNotOwner"
                  class="absolute bottom-full mb-2 hidden w-32 group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
                >
                  You need to be board owner to perform this action.
                </div>
              </div>
            </td>

            <td v-if="collab.status === `PENDING`" class="itbkk-collab-remove">
              <div class="ml-16 relative group inline-block">
                <button
                  :disabled="disabledIfNotOwner"
                  class="btn rounded-full w-14 h-8 sm:w-10 sm:h-10 md:w-14 md:h-12 md:rounded-md"
                  @click="openCancelModal(collab.oid, collab.name)"
                >
                  Cancel
                </button>
                <!-- Tooltip -->
                <div
                  v-if="disabledIfNotOwner"
                  class="absolute bottom-full mb-2 hidden w-32 group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
                >
                  You need to be board owner to perform this action.
                </div>
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
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4"
  >
    <div class="bg-white p-6 rounded-lg shadow-lg w-full sm:max-w-lg">
      <h2 class="text-xl font-semibold mb-4 text-center">
        Change Access Right
      </h2>
      <p class="mb-6 text-center">
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

  <!-- Modal Sending Email -->
  <div
    v-if="loadingEmail"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="bg-white p-8 rounded-lg shadow-lg max-w-md text-center">
      <div class="flex justify-center mb-4">
        <span class="loading loading-dots loading-lg"></span>
      </div>
      <p class="text-gray-700 mb-6">Sending email, please wait...</p>
    </div>
  </div>

  <!-- Modal To Invitations -->
  <div
    v-if="NotSendEmail"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div
      class="relative bg-white p-8 rounded-xl shadow-2xl max-w-md text-center space-y-6 transform transition-all duration-300"
    >
      <button
        class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 focus:outline-none w-10"
        @click="NotSendEmail = false"
      >
        x
      </button>
      <div class="flex justify-center mb-4">
        <span class="text-blue-500 text-4xl">📧</span>
      </div>
      <p class="text-gray-800 font-semibold text-lg leading-relaxed">
        We could not send the email to
        <span class="font-bold">{{ collabIsNotSentEmail?.name }}</span
        >. he/she can accept the invitation at:
      </p>
      <div
        class="border border-gray-300 rounded-lg p-3 bg-gray-50 hover:bg-gray-100 transition-colors duration-200 w-full break-words"
      >
        <a
          class="text-blue-600 font-sm underline hover:text-blue-800 transition-colors duration-200"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ `${link}board/${boardId}/collab/invitations` }}
        </a>
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

<style scoped>
@media (max-width: 768px) {
  .breadcrumbs ul {
    flex-direction: column;
    align-items: center;
  }
  .itbkk-button-home {
    width: 100%;
    margin-bottom: 5px;
  }
  .itbkk-collaborator-add {
    width: 30%;
    margin-bottom: 5px;
  }
  .table {
    font-size: 0.875rem;
  }
  .itbkk-collab-remove {
    text-align: center;
  }
}
</style>
