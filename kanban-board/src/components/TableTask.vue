<script setup>
import { ref } from "vue"
import EditTask from "../components/EditTask.vue"
import { getItemById } from "../libs/fetchUtils"
import { useTaskStore } from "../stores/taskStore"
import { useStatusStore } from "../stores/statusStore"
import router from "@/router"
import { useModalStore } from "../stores/modal"
import Delete from "../components/Delete.vue"
import { RouterLink } from "vue-router"
import AlertComponent from "./Alert.vue"

const showEditModal = ref(false)
const task = ref()
const mytasks = useTaskStore()
const myStatus = useStatusStore()

const myTask = useTaskStore()

const modal = useModalStore()

const modalAlert = ref({ message: "", type: "", modal: false })
myTask.showNavbar = true

const closeCancle = () => {
  showEditModal.value = false
  router.go(-1)
}
const closeEditModal = (statusCode) => {
  if (statusCode === 200) {
    showEditModal.value = false
    router.go(-1)
    modalAlert.value = {
      message: "The task has been updated",
      type: "success",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }

  if (statusCode === 400) {
    modalAlert.value = {
      message: "There are some fields that exceed the limit.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")

    if (statusCode === 404) {
      showEditModal.value = false
      router.go(-1)
      modalAlert.value = {
        message: "An error has occurred, the task does not exist.",
        type: "error",
        modal: true,
      }
      setTimeout(() => {
        modalAlert.value.modal = false
      }, "2500")
    }
  }
}

const openModal = async (taskId) => {
  if (taskId) {
    const data = await getItemById(
      `${import.meta.env.VITE_API_URL}tasks`,
      taskId
    )
    if (data.status === 404) {
      modalAlert.value = {
        message: "An error has occurred, the task does not exist.",
        type: "error",
        modal: true,
      }
      setTimeout(() => {
        modalAlert.value.modal = false
      }, "2500")
      mytasks.removeTasks(modal.deleteId)
      router.go(-1)
    } else {
      task.value = data
      showEditModal.value = true
    }
  }
}

const openDeleteModal = (id, title, index) => {
  modal.showDelete = true // เปิด Modal Delete
  modal.deleteId = id
  modal.deleteTitle = title
  modal.indexNumber = index + 1
}
</script>

<template>
  <EditTask
    @closeModal="closeCancle"
    @closeEditTask="closeEditModal"
    :showModal="showEditModal"
    :task="task"
  />
  <Delete />

  <!-- Task Table -->
  <div class="flex flex-col items-center mt-16 mb-20">
    <div class="flex justify-between w-4/5">
      <div class="font-bold text-4xl text-blue-400 m-2">My Task</div>
      <RouterLink :to="{ name: 'tableStatus' }">
        <button class="btn text-l">Manage Status</button>
      </RouterLink>
    </div>

    <div class="overflow-x-auto border border-blue-400 rounded-md w-4/5 mt-4">
      <table class="table">
        <thead class="bg-blue-400">
          <tr class="text-white text-sm">
            <th></th>
            <th class="pl-20">Title</th>
            <th class="pl-20">Assignees</th>
            <th class="pl-20">Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody class="bg-white" v-if="myTask.getTasks().length > 0">
          <tr
            v-for="(task, index) in myTask.getTasks()"
            :key="task.id"
            class="itbkk-item"
          >
            <th class="text-blue-400 pl-20">{{ index + 1 }}</th>
            <td class="itbkk-title itbkk-button-action itbkk-button-edit pl-20">
              <router-link :to="{ name: 'edit', params: { id: task.id } }">
                <button
                  @click="openModal(task.id)"
                  class="btn btn-ghost h-auto"
                  style="text-align: left"
                >
                  {{ task.title }}<img src="/icons/pen.png" class="w-4" />
                </button>
              </router-link>
            </td>
            <td
              class="itbkk-assignees pl-20"
              :style="{ 'font-style': task.assignees ? 'medium' : 'italic' }"
            >
              <p v-if="task.assignees">
                {{ task.assignees }}
              </p>
              <p v-else class="text-gray-500 font-medium">Unassigned</p>
            </td>
            <td class="itbkk-status pl-20">
              <div
                class="rounded-md p-2 text-black w-24 text-center"
                :style="{
                  'background-color': myStatus.getStatusColor(task.status),
                }"
              >
                {{ task.status }}
              </div>
            </td>
            <td>
              <button
                @click="openDeleteModal(task.id, task.title, index)"
                v-if="modal.showModal"
                class="itbkk-button-delete btn bg-red-500"
              >
                <img src="/icons/delete.png" class="w-4" />
              </button>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr>
            <td colspan="5" class="text-center py-4 text-gray-500 font-medium">
              No task
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Alert -->
  <AlertComponent
    :message="modalAlert.message"
    :type="modalAlert.type"
    :showAlert="modalAlert.modal"
  />
</template>

<style scoped>
.itbkk-title {
  /* กำหนดความกว้างสูงสุดของ column title */
  max-width: 600px; /* ปรับค่าตามต้องการ */
  word-break: break-all; /* ใช้ให้เกิดการตัดบรรทัด (line break) เมื่อข้อความยาวเกินขอบเขตของคอลัมน์ */
}
</style>
