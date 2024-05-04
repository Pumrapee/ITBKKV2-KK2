<script setup>
import { ref } from "vue"
import EditTask from "../components/EditTask.vue"
import { getItemById } from "../libs/fetchUtils"
import { useTaskStore } from "../stores/taskStore"
import router from "@/router"
import { useModalStore } from "../stores/modal"
import Delete from "../components/Delete.vue"

const showModal = ref(false)
const task = ref()
const mytasks = useTaskStore()

const editFail = ref(false)

const myTask = useTaskStore()

const modal = useModalStore()

const closeModal = () => {
  showModal.value = false
  router.push({ name: "task" })
}

const openModal = async (taskId) => {
  if (taskId) {
    const data = await getItemById(import.meta.env.VITE_BASE_URL, taskId)
    if (data.status === 404) {
      //PBI2
      alert("The requested task does not exist")
      editFail.value = true
      mytasks.removeTasks(modal.deleteId)
      router.push("/task")
    } else {
      task.value = data
      showModal.value = true
    }
  }
}

const reformat = (status) => {
  const statusMap = {
    NO_STATUS: "No Status",
    TO_DO: "To Do",
    DOING: "Doing",
    DONE: "Done",
  }
  return statusMap[status] || status // ถ้าไม่มีค่าใน statusMap ให้ใช้ค่าเดิม
}

const openDeleteModal = (id, title) => {
  modal.showDelete = true // เปิด Modal Delete
  modal.deleteId = id
  modal.deleteTitle = title
}
</script>

<template>
  <EditTask @closeModal="closeModal" :showModal="showModal" :task="task" />
  <Delete />

  <!-- Alert fail Edit-->
  <div v-if="editFail" class="flex justify-center mt-3">
    <div role="alert" class="alert alert-error w-2/3">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="stroke-current shrink-0 h-6 w-6 text-white"
        fill="none"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <span class="text-white">The update was unsuccessful</span>
      <button class="text-white" @click="editFail = false">X</button>
    </div>
  </div>

  <!-- Task Table -->
  <div class="flex flex-col items-center mt-16 mb-20">
    <div class="flex justify-between w-4/5">
      <div class="font-bold text-4xl text-blue-400 m-2">My Task</div>
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
            <th class="text-blue-400 pl-10">{{ index + 1 }}</th>
            <td class="itbkk-title pl-20">
              <button
                @click="openModal(task.id)"
                class="btn btn-ghost h-auto"
                style="text-align: left"
              >
                {{ task.title }}<img src="/icons/pen.png" class="w-4" />
              </button>
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
                class="border rounded-md p-2 text-white w-24"
                :class="{
                  'bg-gray-400 font-semibold':
                    reformat(task.status) === 'No Status',
                  'bg-yellow-400 font-semibold':
                    reformat(task.status) === 'To Do',
                  'bg-purple-400 font-semibold':
                    reformat(task.status) === 'Doing',
                  'bg-green-400 font-semibold':
                    reformat(task.status) === 'Done',
                }"
              >
                {{ reformat(task.status) }}
              </div>
            </td>
            <td class="">
              <button
                @click="openDeleteModal(task.id, task.title)"
                v-if="modal.showModal"
                class="btn bg-red-500"
              >
                <img src="/icons/delete.png" class="w-6" />
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
</template>

<style scoped>
.itbkk-title {
  /* กำหนดความกว้างสูงสุดของ column title */
  max-width: 600px; /* ปรับค่าตามต้องการ */
  word-break: break-all; /* ใช้ให้เกิดการตัดบรรทัด (line break) เมื่อข้อความยาวเกินขอบเขตของคอลัมน์ */
}
</style>
