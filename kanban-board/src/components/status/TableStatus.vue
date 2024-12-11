<script setup>
import { useStatusStore } from "@/stores/statusStore"
import { useTaskStore } from "@/stores/taskStore"
import { useAuthStore } from "@/stores/loginStore"
import { useBoardStore } from "@/stores/boardStore"
import {
  getItemById,
  findStatus,
  editItem,
  getItems,
  addItem,
  deleteItemById,
  deleteItemByIdToNewId,
  checkAndRefreshToken,
} from "@/libs/fetchUtils"
import { showAlert } from "../../libs/alertUtils"
import { ref, onMounted, watch } from "vue"
import AddEditStatus from "@/components/status/AddEditStatus.vue"
import DeleteStatus from "@/components/status/DeleteStatus.vue"
import ExpireToken from "../toast/ExpireToken.vue"
import router from "@/router"
import { useRoute } from "vue-router"

//store
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
const refreshToken = ref(localStorage.getItem("refreshToken"))
const disabledIfNotOwner = ref(false)
const nameOwnerBoard = ref()

// user name login
const userName = localStorage.getItem("user")

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
  function validateBoardAccess(isOwner, userOid) {
    if (isOwner) {
      return false
    }
    const collab = myBoard.getCollabs().find((collab) => collab.oid === userOid)
    let accessRight
    if (collab !== undefined) {
      accessRight = collab.accessRight
    }
    if (accessRight !== undefined) {
      // If the user has WRITE access, they can manage tasks and statuses
      if (accessRight === "WRITE" && collab.status === "MEMBER") {
        return false
      }
    }
    return true
  }
  if (
    validateBoardAccess(
      nameOwnerBoard.value === userName,
      localStorage.getItem("oid")
    )
  ) {
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
        router.push({ name: "TaskNotFound" })
      } else {
        myStatus.addStatus(listStatus)
      }
    }
  }

  if (checkToken.statusCode === 401) {
    expiredToken.value = true
  }
})

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
      showAlert("An error has occurred, the status does not exist.", "error")
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
    name: "",
    description: "",
    color: "#FFFFFF",
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
      countTask: countTask.length,
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
          color: status.color,
        }
      )

      if (statusCode === 200) {
        myStatus.updateStatus(editedItem)
        const listTasks = await getItems(
          `${import.meta.env.VITE_API_URL}v3/boards/${boardId.value}/tasks`
        )
        myTask.clearTask()
        myTask.addTasks(listTasks)
        showAlert("The status has been updated", "success")
      } else {
        // 400 , 404
        myStatus.removeStatus(editedItem.id)
        showAlert("An error has occurred, the status does not exist.", "error")
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
        showAlert("The status has been added", "success")
        myStatus.getStatus()
      } else {
        // 400 , 500
        showAlert("An error has occurred, the status does not exist.", "error")
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
        showAlert("The status has been deleted", "success")
      }

      if (deleteItem === 400) {
        myStatus.removeStatus(statusDetail.value.id)
        showAlert("An error has occurred, the status does not exist.", "error")
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
          "success"
        )
      }
      if (newStatus === 400) {
        myStatus.removeStatus(filteredStatus)
        showAlert("An error has occurred, the status does not exist.", "error")
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
  router.push({ name: "tableStatus" })
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
          router.push({ name: "TaskNotFound" })
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
  <div class="flex flex-col items-center mt-32 md:ml-40 mb-10 px-10 md:px-12">
    <!-- Navigation -->
    <div class="flex flex-wrap justify-between items-center w-full md:w-8/12">
      <div class="text-sm breadcrumbs text-black">
        <ul class="flex flex-wrap">
          <li class="itbkk-button-home">
            <RouterLink :to="{ name: 'task' }"> Home</RouterLink>
          </li>
          <li>Task Status</li>
        </ul>
      </div>
      <div class="flex items-center mt-4 md:mt-0">
        <RouterLink :to="{ name: 'task' }">
          <button
            class="itbkk-button-home btn mr-2 bg-black text-white text-sm md:text-base w-full md:w-auto mb-2 md:mb-0"
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
              class="itbkk-button-add btn bg-black text-white ml-2 w-full md:w-auto itbkk-button-add-desktop"
            >
              <img src="/icons/plus.png" class="w-4" />
            </button>

            <!-- Tooltip -->
            <div
              v-if="disabledIfNotOwner"
              class="absolute bottom-full w-32 mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
            >
              You need to be board owner or has write access to perform this
            </div>
          </div>
        </RouterLink>
      </div>
    </div>

    <!-- Status Table -->
    <div
      class="overflow-x-auto border border-black rounded-md w-full md:w-8/12 mt-4"
    >
      <table class="table w-full text-xs md:text-sm">
        <thead class="bg-black">
          <tr class="text-white">
            <th class="px-4 py-2 sm:pl-20 text-sm hidden md:table-cell">No.</th>
            <th class="px-4 py-2 sm:pl-20 text-sm">Name</th>
            <th class="px-4 py-2 sm:pl-18 text-sm">Description</th>
            <th class="px-4 py-2 sm:pl-20 text-sm">Action</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(task, index) in myStatus.getStatus()"
            :key="task.id"
            class="itbkk-item"
          >
            <th class="text-black sm:pl-20 hidden md:table-cell">
              {{ index + 1 }}
            </th>

            <td class="itbkk-status-name px-4 py-2 w-1/3">
              <p
                class="shadow-md rounded-full h-auto max-w-40 font-medium text-center p-3 break-words"
                :style="{ 'background-color': task.color }"
              >
                {{ task.name }}
              </p>
            </td>

            <td class="itbkk-status-description px-4 py-2">
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
              class="flex items-center px-4 py-2"
            >
              <div class="mr-2 itbkk-button-edit">
                <router-link
                  :to="{ name: 'EditStatus', params: { statusId: task.id } }"
                >
                  <div class="relative group inline-block">
                    <button
                      :disabled="disabledIfNotOwner"
                      @click="openEditStatus(task.id)"
                      class="btn btn-ghost bg-yellow-200 w-12 h-12 sm:w-10 sm:h-10 md:w-12 md:h-12"
                    >
                      <img src="/icons/pen.png" class="w-4" />
                    </button>

                    <!-- Tooltip -->
                    <div
                      v-if="disabledIfNotOwner"
                      class="absolute bottom-full w-28 mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
                    >
                      You need to be board owner or has write access to perform
                      this
                    </div>
                  </div>
                </router-link>
              </div>
              <div class="relative group inline-block">
                <button
                  :disabled="disabledIfNotOwner"
                  class="itbkk-button-delete btn bg-red-500 w-12 h-12 sm:w-10 sm:h-10 md:w-12 md:h-12"
                  @click="openDeleteModal(task.id, task.name)"
                >
                  <img src="/icons/delete.png" class="w-4" />
                </button>

                <!-- Tooltip -->
                <div
                  v-if="disabledIfNotOwner"
                  class="absolute bottom-full w-28 mb-2 hidden group-hover:block opacity-0 group-hover:opacity-100 transition-opacity bg-gray-700 text-white text-xs rounded py-1 px-2 z-10"
                >
                  You need to be board owner or has write access to perform this
                </div>
              </div>
            </td>
            <td
              v-else="task.name !== 'No Status' && task.name !== 'Done'"
              class="px-4 py-2"
            ></td>
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

  <ExpireToken v-if="expiredToken" />
</template>

<style scoped>
.itbkk-status-description {
  max-width: 350px;
  word-break: break-all;
}

@media (max-width: 768px) {
  .breadcrumbs ul {
    flex-direction: column;
  }
  .itbkk-button-home {
    width: 100%;
    margin-bottom: 5px;
  }
  .itbkk-button-add {
    width: 100%;
    margin-bottom: 5px;
  }
  .table {
    font-size: 0.875rem;
  }
}

@media (min-width: 768px) {
  /* ปรับแต่งปุ่ม add สำหรับหน้า desktop ให้เป็นวงกลม */
  .itbkk-button-add-desktop {
    border-radius: 50%;
    width: 50px;
    height: 50px;
  }
}
</style>
