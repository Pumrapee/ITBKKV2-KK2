<script setup>
import { ref } from "vue"
import { useTaskStore } from "../stores/taskStore"
import { useStatusStore } from "../stores/statusStore"
import AddStatus from "./AddStatus.vue"
import EditStatus from "./EditStatus.vue"
import router from "@/router"
import { getItemById, findStatus } from "../libs/fetchUtils"
import AlertComponent from "./Alert.vue"
import DeleteStatus from "./DeleteStatus.vue"

const myTask = useTaskStore()
const myStatus = useStatusStore()
const showAddModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const showTransferModal = ref(false)
const statusItems = ref()
const statusDetail = ref()
const modalAlert = ref({ message: "", type: "", modal: false })

//ไม่โชว์ add delete บน navbar
myTask.showNavbar = false

const openAddStatus = () => {
  showAddModal.value = true
}

const openDeleteStatus = async (id, name) => {
  // 200 tranfer , 404 confrim
  const Showstatus = await findStatus(
    `${import.meta.env.VITE_API_URL}tasks/status`,
    id
  )
  console.log(Showstatus)
  if (Showstatus === 200) {
    showTransferModal.value = true
  }
  if (Showstatus === 404) {
    showDeleteModal.value = true
  }
  statusDetail.value = {
    id: id,
    name: name,
  }
}

const closeCancle = () => {
  if (showDeleteModal.value === true) {
    showDeleteModal.value = false
  }
  if (showTransferModal.value === true) {
    showTransferModal.value = false
  }

  if (showAddModal.value === true || showEditModal.value === true) {
    showAddModal.value = false
    showEditModal.value = false
    router.go(-1)
  }
}

const closeAddModal = (statusCode) => {
  if (statusCode === 201) {
    showAddModal.value = false
    router.go(-1)
    modalAlert.value = {
      message: "The status has been added",
      type: "success",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  } else {
    modalAlert.value = {
      message: "An error has occurred, the status could not be added.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }
}

const closeEditModal = (statusCode) => {
  if (statusCode === 200) {
    showEditModal.value = false
    router.go(-1)
    modalAlert.value = {
      message: "The status has been updated",
      type: "success",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }

  if (statusCode === 400) {
    modalAlert.value = {
      message: "An error has occurred, the status does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }

  if (statusCode === 404) {
    showEditModal.value = false
    router.go(-1)
    modalAlert.value = {
      message: "An error has occurred, the status does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }
}

const closeDeleteModal = (statusCode) => {
  if (statusCode === 200) {
    showDeleteModal.value = false
    modalAlert.value = {
      message: "The status has been deleted",
      type: "success",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }
  if (statusCode === 404) {
    showDeleteModal.value = false
    modalAlert.value = {
      message: "An error has occurred, the status does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }
}

const closeTransfereModal = (statusCode) => {
  if (statusCode === 200) {
    showTransferModal.value = false
    modalAlert.value = {
      message:
        "The task(s) have been transferred and the status has been deleted",
      type: "success",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }

  if (statusCode === 404) {
    showTransferModal.value = false
    modalAlert.value = {
      message: "An error has occurred, the status does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }
}

const openEditStatus = async (idStatus) => {
  if (idStatus) {
    const statusItem = await getItemById(
      `${import.meta.env.VITE_API_URL}statuses`,
      idStatus
    )
    if (statusItem.status === 404) {
      modalAlert.value = {
        message: "An error has occurred, the status does not exist.",
        type: "error",
        modal: true,
      }
      setTimeout(() => {
        modalAlert.value.modal = false
      }, "2500")
      myStatus.removeStatus(idStatus)
      router.go(-1)
    } else {
      statusItems.value = statusItem
      showEditModal.value = true
    }
  }
}
</script>

<template>
  <div class="flex flex-col items-center mt-16 mb-20">
    <!-- Navigation -->
    <div class="flex justify-between w-4/5">
      <div class="flex text-sm breadcrumbs text-blue-400">
        <ul>
          <li class="itbkk-button-home">
            <RouterLink :to="{ name: 'task' }"> Home</RouterLink>
          </li>
          <li>Task Status</li>
        </ul>
      </div>
      <RouterLink :to="{ name: 'AddStatus' }">
        <button @click="openAddStatus" class="itbkk-button-home btn">
          Add status
        </button>
      </RouterLink>
    </div>

    <!-- Task Table -->
    <div class="overflow-x-auto border border-blue-400 rounded-md w-4/5 mt-4">
      <table class="table">
        <thead class="bg-blue-400">
          <tr class="text-white text-sm">
            <th></th>
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
                class="h-auto max-w-40 font-medium rounded-md text-center p-3 break-all"
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

            <!-- ใส่ v-if เพื่อตรวจสอบว่า index เท่ากับ 0 หรือไม่ -->
            <td v-if="index !== 0" class="ml-10 flex">
              <div class="mr-2">
                <router-link
                  :to="{ name: 'EditStatus', params: { id: task.id } }"
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
                  @click="openDeleteStatus(task.id, task.name)"
                >
                  <img src="/icons/delete.png" class="w-4" />
                </button>
              </div>
            </td>
            <!-- ถ้า index = 0 ไม่ขึ้น icon -->
            <td v-else="index === 0" class="h-20"></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <EditStatus
    :showEditStatus="showEditModal"
    @closeEditStatus="closeEditModal"
    @closeCancleStatus="closeCancle"
    :taskStatus="statusItems"
  />
  <AddStatus
    :showAddStatus="showAddModal"
    @closeAddStatus="closeAddModal"
    @closeCancleStatus="closeCancle"
  />
  <DeleteStatus
    :showDeleteStatus="showDeleteModal"
    :showTransferModal="showTransferModal"
    :deatailStatus="statusDetail"
    @closeCancle="closeCancle"
    @closeDeleteStatus="closeDeleteModal"
    @closeTransferStatus="closeTransfereModal"
  />
  <AlertComponent
    :message="modalAlert.message"
    :type="modalAlert.type"
    :showAlert="modalAlert.modal"
  />
</template>

<style scoped>
.itbkk-assignees {
  /* กำหนดความกว้างสูงสุดของ column title */
  max-width: 400px; /* ปรับค่าตามต้องการ */
  word-break: break-all; /* ใช้ให้เกิดการตัดบรรทัด (line break) เมื่อข้อความยาวเกินขอบเขตของคอลัมน์ */
}
</style>
