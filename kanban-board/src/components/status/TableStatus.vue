<script setup>
import { useStatusStore } from '@/stores/statusStore'
import { useTaskStore } from '@/stores/taskStore'
import { useAuthStore } from '@/stores/loginStore'
import { useBoardStore } from '@/stores/boardStore'
import {
  getItemById,
  findStatus,
  editItem,
  getItems,
  addItem,
  deleteItemById,
  deleteItemByIdToNewId,
  checkAndRefreshToken
} from '@/libs/fetchUtils'
import { ref, onMounted, watch } from 'vue'
import AddEditStatus from '@/components/status/AddEditStatus.vue'
import DeleteStatus from '@/components/status/DeleteStatus.vue'
import AlertComponent from '@/components/toast/Alert.vue'
import ExpireToken from '../toast/ExpireToken.vue'
import router from '@/router'
import { useRoute } from 'vue-router'

const myStatus = useStatusStore()
const myTask = useTaskStore()
const myUser = useAuthStore()
const myBoard = useBoardStore()
const statusItems = ref({})
const openModal = ref()
const editMode = ref(false)
const statusDetail = ref()
const showTransferModal = ref(false)
const showDeleteModal = ref(false)
const route = useRoute()
const boardId = ref(route.params.id)
const expiredToken = ref(false)
const refreshToken = ref(localStorage.getItem('refreshToken'))
const modalAlert = ref({ message: '', type: '', modal: false })

const disabledIfNotOwner = ref(false)
const nameOwnerBoard = ref()

// user name login
const userName = localStorage.getItem('user')

onMounted(async () => {
  myUser.setToken()

  const boardIdNumber = await getItemById(
    `${import.meta.env.VITE_API_URL}v3/boards`,
    boardId.value
  )

  //Collab
  if (myBoard.getCollabs().length === 0) {
      const collabList = await getItems(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/collabs`
    )
    if (collabList === 401) {
      expiredToken.value = true
    } else {
      if (myBoard.getCollabs().length === 0) {
        collabList.sort((a, b) => new Date(a.addedOn) - new Date(b.addedOn))

        myBoard.addCollabs(collabList)
      }
    }
  }

  nameOwnerBoard.value = boardIdNumber.owner.name
  function validateBoardAccess(isOwner, accessRight) {
    if (accessRight !== undefined) {
      // If the user is the owner, they have full access
      if (isOwner) {
          return false;
      }

      // If the user has WRITE access, they can manage tasks and statuses
      if (accessRight === "WRITE") {
          return false;
      }
    }
    return true;
  }
  
  if (validateBoardAccess(nameOwnerBoard.value === userName ,myBoard.getCollabs().find(collab => collab.oid === localStorage.getItem('oid')).accessRight)) {
    disabledIfNotOwner.value = true
  }

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)

    if (myStatus.getStatus().length === 0) {
      const listStatus = await getItems(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`
      )
      //401
      if (listStatus === 401) {
        expiredToken.value = true
      } else if (listStatus.status === 404) {
        router.push({ name: 'TaskNotFound' })
      } else {
        myStatus.addStatus(listStatus)
      }
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
})

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

//เปิด Modal
const openEditStatus = async (idStatus) => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)

    const statusItem = await getItemById(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`,
      idStatus
    )

    if (statusItem.status === 404) {
      showAlert('An error has occurred, the status does not exist.', 'error')
      myStatus.removeStatus(idStatus)
      router.go(-1)
    } else {
      statusItems.value = statusItem
      openModal.value = true
      editMode.value = true
    }

    if (statusItem === 401) {
      expiredToken.value = true
      openModal.value = false
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    openModal.value = false
  }
}

const openModalAdd = () => {
  openModal.value = true
  statusItems.value = {
    id: undefined,
    name: '',
    description: '',
    color: '#FFFFFF'
  }
}

const openDeleteModal = async (id, name) => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)
    const showStatus = await findStatus(
      `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks/status`,
      id
    )

    const countTask = myTask.getTasks().filter((listTask) => {
      const statusName = myStatus.getStatus().find((listStatus) => {
        return listStatus.id === id
      })
      return listTask.status === statusName.name
    })

    if (showStatus === 200) {
      showTransferModal.value = true
    }
    if (showStatus === 404) {
      showDeleteModal.value = true
    }
    if (showStatus === 401) {
      // showTransferModal.value = false
      // showDeleteModal.value = false
      expiredToken.value = true
    }

    statusDetail.value = {
      id: id,
      name: name,
      countTask: countTask.length
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    // showTransferModal.value = false
    // showDeleteModal.value = false
  }
}

//ปิด Modal
const closeAddEdit = async (status) => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)

    if (editMode.value) {
      const { editedItem, statusCode } = await editItem(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`,
        status.id,
        {
          name: status.name,
          description: status.description,
          color: status.color
        }
      )

      if (statusCode === 200) {
        myStatus.updateStatus(editedItem)
        const listTasks = await getItems(
          `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks`
        )
        myTask.clearTask()
        myTask.addTasks(listTasks)
        showAlert('The status has been updated', 'success')
      } else {
        // 400 , 404
        myStatus.removeStatus(editedItem.id)
        showAlert('An error has occurred, the status does not exist.', 'error')
      }

      if (statusCode === 401) {
        expiredToken.value = true
        openModal.value = false
        editMode.value = false
      }
    }

    if (!editMode.value) {
      const { newTask, statusCode } = await addItem(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`,
        status
      )

      if (statusCode === 201) {
        myStatus.addOneStatus(newTask)
        showAlert('The status has been added', 'success')
        myStatus.getStatus()
      } else {
        // 400 , 500
        showAlert('An error has occurred, the status does not exist.', 'error')
      }

      if (statusCode === 401) {
        expiredToken.value = true
        openModal.value = false
        editMode.value = false
      }
    }
    openModal.value = false
    editMode.value = false
    router.go(-1)
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    openModal.value = false
    editMode.value = false
  }
}

const closeDeleteStatus = async (selectedStatus, filteredStatus) => {
  myUser.setToken()

  const checkToken = await checkAndRefreshToken(
    `${import.meta.env.VITE_API_URL}token`,
    myUser.token,
    refreshToken.value
  )

  if (checkToken.statusCode === 200) {
    //กรณีที่ token หมดอายุ ให้ต่ออายุ token
    myUser.setNewToken(checkToken.accessNewToken)

    if (showDeleteModal.value) {
      const deleteItem = await deleteItemById(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`,
        statusDetail.value.id
      )

      if (deleteItem === 200) {
        myStatus.removeStatus(statusDetail.value.id)
        showAlert('The status has been deleted', 'success')
      }

      if (deleteItem === 400) {
        myStatus.removeStatus(statusDetail.value.id)
        showAlert('An error has occurred, the status does not exist.', 'error')
      }

      if (deleteItem === 401) {
        expiredToken.value = true
      }
    }

    if (showTransferModal.value) {
      const newStatus = await deleteItemByIdToNewId(
        `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`,
        statusDetail.value.id,
        selectedStatus
      )

      if (newStatus === 200) {
        myStatus.removeStatus(statusDetail.id)
        const listTasks = await getItems(
          `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks`
        )
        // หลัง tranfer สำเร้จ ให้ค่าใน task status เปลี่ยน
        myTask.clearTask()
        myTask.addTasks(listTasks)

        const listStatus = await getItems(
          `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`
        )
        myStatus.clearStatus()
        myStatus.addStatus(listStatus)
        showAlert(
          `${statusDetail.value.countTask} task(s) have been transferred and the status has been deleted`,
          'success'
        )
      }
      if (newStatus === 400) {
        myStatus.removeStatus(filteredStatus)
        showAlert('An error has occurred, the status does not exist.', 'error')
      }

      if (newStatus === 401) {
        expiredToken.value = true
      }
    }
    showTransferModal.value = false
    showDeleteModal.value = false
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
    showTransferModal.value = false
    showDeleteModal.value = false
  }
}

const closeModal = () => {
  openModal.value = false
  showTransferModal.value = false
  showDeleteModal.value = false
  router.push({ name: 'tableStatus' })
}

// route path ถ้าไม่มี id นั้น
watch(
  () => route.params.statusId,
  async (newId, oldId) => {
    myUser.setToken()

    const checkToken = await checkAndRefreshToken(
      `${import.meta.env.VITE_API_URL}token`,
      myUser.token,
      refreshToken.value
    )

    if (checkToken.statusCode === 200) {
      //กรณีที่ token หมดอายุ ให้ต่ออายุ token
      myUser.setNewToken(checkToken.accessNewToken)

      if (newId !== undefined) {
        const res = await getItemById(
          `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/statuses`,
          newId
        )
        if (res.status === 404) {
          router.push({ name: 'TaskNotFound' })
        }
      }
    }

    if (checkToken.statusCode === 401) {
      expiredToken.value = true
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="flex flex-col items-center mt-32 mb-20 ml-30">
    <!-- Navigation -->
    <div class=" flex justify-between w-3/5 ml-52">
      <div class="flex text-sm breadcrumbs text-black">
        <ul>
          <li class="itbkk-button-home">
            <RouterLink :to="{ name: 'task' }"> Home</RouterLink>
          </li>
          <li>Task Status</li>
        </ul>
      </div>
      <div class="flex items-center">
        <RouterLink :to="{ name: 'task' }">
          <button
            @click="openAddStatus"
            class="itbkk-button-home btn mr-1 bg-black text-white"
          >
            <img src="/icons/home.png" class="w-4" />
            Home
          </button>
        </RouterLink>
        <RouterLink :to="{ name: 'AddStatus' }">
          <div class="relative group inline-block">
            <button
              :disabled="disabledIfNotOwner"
              @click="openModalAdd"
              class="itbkk-button-add btn btn-circle border-black0 bg-black text-white ml-2"
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
        </RouterLink>
      </div>
    </div>

    <!-- Status Table -->
    <div
      class=" overflow-x-auto border border-black rounded-md w-3/5 mt-4 ml-52"
    >
      <table class="table">
        <thead class="bg-black">
          <tr class="text-white text-sm">
            <th class="pl-20">No.</th>
            <th class="pl-20">Name</th>
            <th class="pl-20">Description</th>
            <th class="pl-20">Action</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(task, index) in myStatus.getStatus()"
            :key="task.id"
            class="itbkk-item"
          >
            <th class="text-black pl-20">{{ index + 1 }}</th>

            <td class="itbkk-status-name pl-20 w-1/3">
              <p
                class="shadow-md rounded-full h-auto max-w-40 font-medium text-center p-3 break-all"
                :style="{ 'background-color': task.color }"
              >
                {{ task.name }}
              </p>
            </td>

            <td class="itbkk-status-description pl-20">
              <p v-if="task.description">
                {{ task.description }}
              </p>
              <p v-else class="text-gray-500 font-medium italic">
                No Description Provided
              </p>
            </td>

            <!-- ใส่ v-if เพื่อตรวจสอบและแสดงปุ่มแก้ไขและลบเมื่อ task.name ไม่เท่ากับ 'No Status' หรือ 'Done' -->
            <td
              v-if="task.name !== 'No Status' && task.name !== 'Done'"
              class="ml-10 flex"
            >
              <div class="mr-2 itbkk-button-edit">
                <router-link
                  :to="{ name: 'EditStatus', params: { statusId: task.id } }"
                >
                  <div class="relative group inline-block">
                    <button
                      :disabled="disabledIfNotOwner"
                      @click="openEditStatus(task.id)"
                      class="btn btn-ghost h-auto bg-yellow-100"
                    >
                      <img src="/icons/pen.png" class="w-4 ml-2" />
                    </button>

                    <!-- Tooltip -->
                    <div
                      v-if="disabledIfNotOwner"
                      class="absolute bottom-full mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
                    >
                      You need to be board owner to perform this action.
                    </div>
                  </div>
                </router-link>
              </div>
              <div class="relative group inline-block">
                <button
                  :disabled="disabledIfNotOwner"
                  class="itbkk-button-delete btn bg-red-500"
                  @click="openDeleteModal(task.id, task.name)"
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
            <td v-else="task.name !== 'No Status' && task.name !== 'Done'"></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <AddEditStatus
    :showModal="openModal"
    :taskStatus="statusItems"
    :editModeModal="editMode"
    @closeModal="closeModal"
    @saveAddEdit="closeAddEdit"
  />

  <DeleteStatus
    :showDeleteStatus="showDeleteModal"
    :showTransferModal="showTransferModal"
    :detailStatus="statusDetail"
    @closeModal="closeModal"
    @closeDeleteStatus="closeDeleteStatus"
  />

  <!-- Toast -->
  <AlertComponent
    :message="modalAlert.message"
    :type="modalAlert.type"
    :showAlert="modalAlert.modal"
  />

  <ExpireToken v-if="expiredToken" />
</template>

<style scoped>
.itbkk-status-description {
  /* กำหนดความกว้างสูงสุดของ column title */
  max-width: 350px; /* ปรับค่าตามต้องการ */
  word-break: break-all; /* ใช้ให้เกิดการตัดบรรทัด (line break) เมื่อข้อความยาวเกินขอบเขตของคอลัมน์ */
}
.bounce-in-top {
  -webkit-animation: bounce-in-top 1.1s both;
  animation: bounce-in-top 1.1s both;
}
@-webkit-keyframes bounce-in-top {
  0% {
    -webkit-transform: translateY(-500px);
    transform: translateY(-500px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
    opacity: 0;
  }
  38% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
    opacity: 1;
  }
  55% {
    -webkit-transform: translateY(-65px);
    transform: translateY(-65px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  72% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
  81% {
    -webkit-transform: translateY(-28px);
    transform: translateY(-28px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  90% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
  95% {
    -webkit-transform: translateY(-8px);
    transform: translateY(-8px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  100% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
}
@keyframes bounce-in-top {
  0% {
    -webkit-transform: translateY(-500px);
    transform: translateY(-500px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
    opacity: 0;
  }
  38% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
    opacity: 1;
  }
  55% {
    -webkit-transform: translateY(-65px);
    transform: translateY(-65px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  72% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
  81% {
    -webkit-transform: translateY(-28px);
    transform: translateY(-28px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  90% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
  95% {
    -webkit-transform: translateY(-8px);
    transform: translateY(-8px);
    -webkit-animation-timing-function: ease-in;
    animation-timing-function: ease-in;
  }
  100% {
    -webkit-transform: translateY(0);
    transform: translateY(0);
    -webkit-animation-timing-function: ease-out;
    animation-timing-function: ease-out;
  }
}
</style>
