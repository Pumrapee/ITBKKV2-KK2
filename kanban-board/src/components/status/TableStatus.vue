<script setup>
import { useStatusStore } from "@/stores/statusStore"
import { useTaskStore } from "@/stores/taskStore"
import {
  getItemById,
  findStatus,
  editItem,
  getItems,
  addItem,
  deleteItemById,
  deleteItemByIdToNewId,
} from "@/libs/fetchUtils"
import { ref, onMounted, watch } from "vue"
import AddEditStatus from "@/components/status/AddEditStatus.vue"
import DeleteStatus from "@/components/status/DeleteStatus.vue"
import AlertComponent from "@/components/toast/Alert.vue"
import router from "@/router"
import { useRoute } from "vue-router"

const myStatus = useStatusStore()
const myTask = useTaskStore()
const statusItems = ref({})
const openModal = ref()
const editMode = ref(false)
const statusDetail = ref()
const showTransferModal = ref(false)
const showDeleteModal = ref(false)
const boardId = ref()
const route = useRoute()

const modalAlert = ref({ message: "", type: "", modal: false })

onMounted(async () => {
  if (myStatus.getStatus().length === 0) {
    const listStatus = await getItems(
      `${import.meta.env.VITE_API_URL}boards/${boardId.value}/statuses`
    )
    myStatus.addStatus(listStatus)
  }
})

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

//เปิด Modal
const openEditStatus = async (idStatus) => {
  const statusItem = await getItemById(
    `${import.meta.env.VITE_API_URL}statuses`,
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
  const showStatus = await findStatus(
    `${import.meta.env.VITE_API_URL}tasks/status`,
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
  statusDetail.value = {
    id: id,
    name: name,
    countTask: countTask.length,
  }
}

//ปิด Modal
const closeAddEdit = async (status) => {
  if (editMode.value) {
    const { editedItem, statusCode } = await editItem(
      `${import.meta.env.VITE_API_URL}statuses`,
      status.id,
      {
        name: status.name,
        description: status.description,
        color: status.color,
      }
    )

    if (statusCode === 200) {
      myStatus.updateStatus(editedItem)
      const listTasks = await getItems(`${import.meta.env.VITE_API_URL}tasks`)
      myTask.clearTask()
      myTask.addTasks(listTasks)
      showAlert("The status has been updated", "success")
    } else {
      // 400 , 404
      myStatus.removeStatus(editedItem.id)
      showAlert("An error has occurred, the status does not exist.", "error")
    }
  }

  if (!editMode.value) {
    const { newTask, statusCode } = await addItem(
      `${import.meta.env.VITE_API_URL}statuses`,
      status
    )
    if (statusCode === 201) {
      myStatus.addOneStatus(newTask)
      showAlert("The status has been added", "success")
    } else {
      // 400 , 500
      showAlert("An error has occurred, the status does not exist.", "error")
    }
  }
  openModal.value = false
  editMode.value = false
  router.go(-1)
}

const closeDeleteStatus = async (selectedStatus, filteredStatus) => {
  if (showDeleteModal.value) {
    const deleteItem = await deleteItemById(
      `${import.meta.env.VITE_API_URL}statuses`,
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
  }

  if (showTransferModal.value) {
    const newStatus = await deleteItemByIdToNewId(
      `${import.meta.env.VITE_API_URL}statuses`,
      statusDetail.value.id,
      selectedStatus
    )

    if (newStatus === 200) {
      myStatus.removeStatus(statusDetail.id)
      const listTasks = await getItems(`${import.meta.env.VITE_API_URL}tasks`)
      // หลัง tranfer สำเร้จ ให้ค่าใน task status เปลี่ยน
      myTask.clearTask()
      myTask.addTasks(listTasks)

      const listStatus = await getItems(
        `${import.meta.env.VITE_API_URL}statuses`
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
  }

  showTransferModal.value = false
  showDeleteModal.value = false
}

const closeModal = () => {
  openModal.value = false
  showTransferModal.value = false
  showDeleteModal.value = false
  router.push({ name: "tableStatus" })
}

watch(
  () => route.params.id,
  (newId) => {
    boardId.value = newId
  },
  { immediate: true }
)
</script>

<template>
  <div class="flex flex-col items-center mt-16 mb-20 ml-60">
    <!-- Navigation -->
    <div class="bounce-in-top flex justify-between w-3/5 ml-52">
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
          <button
            @click="openModalAdd"
            class="itbkk-button-home btn btn btn-circle border-black0 bg-black text-white ml-2"
          >
            <img src="/icons/plus.png" class="w-4" />
          </button>
        </RouterLink>
      </div>
    </div>

    <!-- Status Table -->
    <div
      class="bounce-in-top overflow-x-auto border border-black rounded-md w-3/5 mt-4 ml-52"
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
        <tbody class="bg-white">
          <tr
            v-for="(task, index) in myStatus.getStatus()"
            :key="task.id"
            class="itbkk-item"
          >
            <th class="text-blue-400 pl-20">{{ index + 1 }}</th>

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
              <div class="mr-2">
                <router-link
                  :to="{ name: 'EditStatus', params: { statusId: task.id } }"
                >
                  <button
                    @click="openEditStatus(task.id)"
                    class="itbkk-button-edit btn btn-ghost h-auto bg-yellow-100"
                  >
                    <img src="/icons/pen.png" class="w-4" />
                  </button>
                </router-link>
              </div>
              <div>
                <button
                  class="itbkk-button-delete btn bg-red-500"
                  @click="openDeleteModal(task.id, task.name)"
                >
                  <img src="/icons/delete.png" class="w-4" />
                </button>
              </div>
            </td>
            <td
              v-else="task.name !== 'No Status' && task.name !== 'Done'"
              class="bg-white"
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

  <!-- Toast -->
  <AlertComponent
    :message="modalAlert.message"
    :type="modalAlert.type"
    :showAlert="modalAlert.modal"
  />
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
