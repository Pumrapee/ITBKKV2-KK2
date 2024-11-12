<script setup>
import { onMounted, ref } from 'vue'
import { useBoardStore } from '@/stores/boardStore'
import { useAuthStore } from '@/stores/loginStore'
import { useRoute } from 'vue-router'
import {
  getItems,
  checkAndRefreshToken,
  addItem,
  getItemById,
  patchItem,
  deleteItemById
} from '@/libs/fetchUtils'
import ExpireToken from '../toast/ExpireToken.vue'
import AddCollabBoard from '../collab/AddCollabBoard.vue'
import Alert from '../toast/Alert.vue'
import RemoveCollaborator from '../collab/RemoveCollaborator.vue'

const route = useRoute()
const boardId = ref(route.params.id)
const myBoard = useBoardStore()
const myUser = useAuthStore()
const boardName = ref()
const expiredToken = ref(false)
const openModal = ref(false)
const modalAlert = ref({ message: '', type: '', modal: false })
const collab = ref()
const refreshToken = ref(localStorage.getItem('refreshToken'))
const showDeleteModal = ref(false)
const collaboratorToRemove = ref({ id: '', name: '' })
const nameOwnerBoard = ref()
const disabledIfNotOwner = ref()

onMounted(async () => {
  myUser.setToken()
  expiredToken.value = false

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
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

  if (nameOwnerBoard.value !== localStorage.getItem('user')) {
    disabledIfNotOwner.value = true
  }
})

const openModalAdd = () => {
  openModal.value = true
  collab.value = {
    email: '',
    accessRight: 'READ'
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
      showAlert('The collaborator has been successfully added.', 'success')
    } else if (statusCode === 401) {
      openModal.value = false
      expiredToken.value = true
    } else if (statusCode === 403) {
      showAlert(
        'You do not have permission to add board collaborator.',
        'error'
      )
      openModal.value = false
    } else if (statusCode === 404) {
      showAlert('The user does not exist.', 'error')
    } else if (statusCode === 409) {
      showAlert('The user is already the collaborator of this board.', 'error')
    } else {
      showAlert('There is a problem. Please try again later.', 'error')
    }
    collab.value = {
      email: '',
      accessRight: 'READ'
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    openModal.value = false
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
    modal: true
  }
  setTimeout(() => {
    modalAlert.value.modal = false
  }, 4000)
}

const openDeleteModal = (id, name) => {
  collaboratorToRemove.value = { id, name }
  showDeleteModal.value = true
}

const confirmRemoveCollaborator = async () => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    const statusCode = await deleteItemById(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs`,
      collaboratorToRemove.value.id
    )

    if (statusCode === 200) {
      showAlert('Collaborator removed successfully.', 'success')
      myBoard.removeCollab(collaboratorToRemove.value.id) // ลบ collaborator ออกจาก list
      showDeleteModal.value = false // ปิด modal
    } else if (statusCode === 403) {
      showAlert(
        'You do not have permission to add board collaborator.',
        'error'
      )
    } else if (statusCode === 404) {
      myBoard.removeCollab(collaboratorToRemove.value.id)
      showDeleteModal.value = false
      showAlert(
        `${collaboratorToRemove.value.name} is not a collaborator.`,
        'error'
      )
    } else if (statusCode === 401) {
      expiredToken.value = true
      showDeleteModal.value = false
    } else {
      showAlert('There is a problem. Please try again later.', 'error')
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    showDeleteModal.value = false
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
    const { statusCode } = await patchItem(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs/${
        collab.oid
      }`,
      { accessRight: newRight }
    )

    if (statusCode === 200) {
      showAlert('Access right updated successfully.', 'success')
    } else if (statusCode === 403) {
      showAlert(
        'You do not have permission to add board collaborator.',
        'error'
      )
    } else {
      showAlert('There is a problem. Please try again later.', 'error')
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
}

const showAccessModal = ref(false)
const selectedCollab = ref(null) // เก็บ collaborator ที่เลือก
const newAccessRight = ref('') // เก็บสิทธิ์ใหม่
const selectedCollabName = ref('')

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
  <div class="bounce-in-top flex flex-col items-center mt-28 lg:ml-36 lg:mt-32 md:mt-12 sm:mt-8 px-4 md:px-10">
    <div class="font-bold text-4xl lg:text-3xl md:text-2xl sm:text-xl text-black self-center pb-5 flex items-center justify-center ml-20 lg:ml-12 md:ml-8 sm:ml-2">
      Collaborator Management
    </div>

    <!-- Navigation -->
    <div class="bounce-in-top flex flex-col lg:flex-row justify-center lg:justify-between w-full lg:w-4/5 flex-wrap mb-4">
      <div class="flex text-sm breadcrumbs text-black mb-4 md:mb-0 justify-center lg:justify-start">
        <ul class="flex flex-wrap">
          <li class="itbkk-board-name font-bold text-base md:text-sm">
            <RouterLink :to="{ name: 'task' }">{{ boardName }}</RouterLink>
          </li>
          <li>Collaborator</li>
        </ul>
      </div>
      <div class="flex items-center justify-center w-full md:w-auto lg:justify-end relative group">
        <RouterLink :to="{ name: 'task' }">
          <button
            @click="openAddStatus"
            class="itbkk-button-home btn bg-black text-white text-sm md:text-base w-full md:w-auto mb-2 md:mb-0"
          >
            <img src="/icons/home.png" class="w-4 mr-1" /> Home
          </button>
        </RouterLink>
        <button
          @click="openModalAdd"
          class="itbkk-collaborator-add btn btn-circle border-black0 bg-black text-white ml-2 w-10 h-10 rounded-full sm:w-8 sm:h-8 md:w-12 md:h-12"
          :disabled="disabledIfNotOwner"
        >
          <img src="/icons/plus.png" class="w-4" />
        </button>

        <!-- Tooltip -->
        <div
          v-if="disabledIfNotOwner"
          class="absolute bottom-full mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
        >
          You need to be board owner to perform this action.
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="border border-black rounded-md w-full lg:w-4/5 mt-4 overflow-x-auto">
      <table class="table w-full text-xs md:text-sm">
        <thead class="bg-black">
          <tr class="text-white">
            <th class="px-4 py-2">No.</th>
            <th class="px-4 py-2">Name</th>
            <th class="px-4 py-2">Email</th>
            <th class="px-4 py-2">Access Right</th>
            <th class="px-4 py-2">Action</th>
          </tr>
        </thead>

        <tbody v-if="myBoard.getCollabs().length > 0">
          <!-- row -->
          <tr v-for="(collab, index) in myBoard.getCollabs()" :key="collab.oid" class="itbkk-item">
            <th class="text-black px-4 py-2">{{ index + 1 }}</th>
            <td class="itbkk-name px-4 py-2 w-1/3 break-words">{{ collab.name }}</td>
            <td class="itbkk-email px-4 py-2 break-words">{{ collab.email }}</td>
            <td class="itbkk-access-right px-4 py-2 relative group">
              <div class="dropdown">
                <label
                  tabindex="0"
                  class="btn btn-ghost shadow-md rounded-full h-auto w-28 font-medium text-center p-2 break-words bg-white"
                  :disabled="disabledIfNotOwner"
                >
                  {{ collab.accessRight }}
                  <svg
                    class="w-4 h-4 text-gray-800 dark:text-white ml-2"
                    aria-hidden="true"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m8 10 4 4 4-4" />
                  </svg>
                </label>
                <ul tabindex="0" class="dropdown-content menu p-2 shadow bg-base-100 rounded-box w-full sm:w-52 z-[1]">
                  <li><a @click="openAccessModal(collab, 'READ')">READ</a></li>
                  <li><a @click="openAccessModal(collab, 'WRITE')">WRITE</a></li>
                </ul>
              </div>

              <!-- Tooltip -->
              <div
                v-if="disabledIfNotOwner"
                class="absolute bottom-full mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
              >
                You need to be board owner to perform this action.
              </div>
            </td>

            <td class="itbkk-collab-remove px-4 py-2 relative group">
              <div class="relative group inline-block">
                <button
                  :disabled="disabledIfNotOwner"
                  class="itbkk-button-delete btn bg-red-500 rounded-full w-8 h-8 sm:w-10 sm:h-10 md:w-12 md:h-12"
                  @click="openDeleteModal(collab.oid, collab.name)"
                >
                  <img src="/icons/delete.png" class="w-4" />
                </button>

                <!-- Tooltip -->
                <div
                  v-if="disabledIfNotOwner"
                  class="absolute bottom-full mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
                >
                  You need to be board owner to perform this action.
                </div>
              </div>
            </td>
          </tr>
        </tbody>

        <tbody v-else>
          <tr>
            <td colspan="5" class="text-center py-4 text-gray-500 font-medium">No board collaborator</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Modal for changing access right -->
  <div v-if="showAccessModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4">
    <div class="bg-white p-6 rounded-lg shadow-lg w-full sm:max-w-lg">
      <h2 class="text-xl font-semibold mb-4 text-center">Change Access Right</h2>
      <p class="mb-6 text-center">
        Do you want to change access right of
        <span class="font-bold text-blue-400">{{ selectedCollabName }}</span>
        to <span class="font-bold text-blue-400">{{ newAccessRight }}</span>?
      </p>
      <div class="flex justify-center space-x-4">
        <button @click="showAccessModal = false" class="px-4 py-2 bg-gray-300 text-white rounded-md hover:bg-gray-400">
          Cancel
        </button>
        <button @click="confirmAccessRightChange" class="px-4 py-2 bg-black text-white rounded-md hover:bg-blue-600">
          Confirm
        </button>
      </div>
    </div>
  </div>

  <AddCollabBoard :showModal="openModal" :collabs="collab" @closeModal="closeModal" @addCollab="closeAddCollab" />
  <RemoveCollaborator
    :showDelete="showDeleteModal"
    :selectedCollabName="collaboratorToRemove.name"
    @confirmRemove="confirmRemoveCollaborator"
    @cancelDelete="showDeleteModal = false"
  />
  <Alert :message="modalAlert.message" :type="modalAlert.type" :showAlert="modalAlert.modal" />
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
    width: 20%;
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
