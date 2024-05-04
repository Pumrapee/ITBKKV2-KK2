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
const editfail = ref(false)

const myTask = useTaskStore()

const closeModal = () => {
  showModal.value = false
  router.push({ name: "task" })
}

const openModal = async (taskId) => {
  if (taskId) {
    const data = await getItemById(import.meta.env.VITE_BASE_URL, taskId)
    if (data.status === 404) {
      //require PBI2
      alert("The requested task does not exist")
      // router.go()
      editfail.value = true
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

const modal = useModalStore()

const openDeleteModal = (id, title) => {
  modal.showDelete = true
  modal.deleteId = id
  modal.deleteTitle = title
}
</script>

<template>
  <EditTask @closeModal="closeModal" :showModal="showModal" :task="task" />
  <Delete />

  <!-- Alert fail Edit-->
  <div v-if="editfail" class="flex justify-center mt-3">
    <div role="alert" class="alert alert-error w-2/3">
      <svg
        xmlns="http://www.w3.org/2000/svg text-white"
        class="stroke-current shrink-0 h-6 w-6"
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
      <span class="text-white">The task does not exist</span>
      <button class="text-white" @click="editfail = false">X</button>
    </div>
  </div>

  <!-- Task Table -->
  <div class="flex flex-col items-center mt-20">
    <div class="flex justify-between w-4/5">
      <div class="font-bold text-4xl text-blue-400 m-2">My Task</div>
    </div>

    <div class="overflow-x-auto border border-blue-400 rounded-md w-4/5 mt-4">
      <table class="table">
        <thead class="bg-blue-400">
          <tr class="text-white text-sm">
            <th></th>
            <th>Title</th>
            <th>Assignees</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody class="bg-white">
          <tr
            v-for="(task, index) in myTask.getTasks()"
            :key="task.id"
            class="itbkk-item"
          >
            <th class="text-blue-400">{{ index + 1 }}</th>
            <td class="itbkk-title">
              <router-link :to="{ name: 'edit', params: { id: task.id } }">
                <button @click="openModal(task.id)" class="btn btn-ghost">
                  {{ task.title
                  }}<img src="/icons/pen.png" class="w-4" /></button
              ></router-link>
            </td>
            <td
              class="itbkk-assignees"
              :style="{ 'font-style': task.assignees ? 'normal' : 'italic' }"
            >
              <p v-if="task.assignees">
                {{ task.assignees }}
              </p>
              <p v-else class="text-gray-500">Unassigned</p>
            </td>
            <td class="itbkk-status">
              <div
                class="border rounded-md p-2 text-white w-24"
                :class="{
                  'bg-gray-400 font-bold':
                    reformat(task.status) === 'No Status',
                  'bg-yellow-400 font-bold': reformat(task.status) === 'To Do',
                  'bg-purple-400 font-bold': reformat(task.status) === 'Doing',
                  'bg-green-400 font-bold': reformat(task.status) === 'Done',
                }"
              >
                {{ reformat(task.status) }}
              </div>
            </td>
            <td>
              <button
                @click="openDeleteModal(task.id, task.title)"
                v-if="modal.showModal"
                class="btn bg-red-500"
              >
                <img src="/icons/delete.png" class="w-5" />
              </button>
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
  max-width: 900px; /* ปรับค่าตามต้องการ */
  word-break: break-all;
}
</style>
