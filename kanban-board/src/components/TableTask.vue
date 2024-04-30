<script setup>
import { ref, onMounted } from "vue"
import AddEditTask from "../components/AddEditTask.vue"
import { getItems, getItemById } from "../libs/fetchUtils"
import router from "@/router"

const showModal = ref(false)
const taskdata = ref([])
const task = ref()

const closeModal = () => {
  showModal.value = false
  router.push({ name: "task" })
}

const openModal = async (taskId) => {
  if (taskId) {
    const data = await getItemById(import.meta.env.VITE_BASE_URL, taskId)
    if (data.status === 404) {
      alert("The requested task does not exist")
      router.go()
    } else {
      task.value = data
      showModal.value = true
    }
  }
}

onMounted(async () => {
  const data = await getItems(import.meta.env.VITE_BASE_URL)
  taskdata.value = data
})

const reformat = (status) => {
  const statusMap = {
    NO_STATUS: "No Status",
    TO_DO: "To Do",
    DOING: "Doing",
    DONE: "Done",
  }
  return statusMap[status] || status // ถ้าไม่มีค่าใน statusMap ให้ใช้ค่าเดิม
}
</script>

<template>
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
          </tr>
        </thead>
        <tbody class="bg-white">
          <tr
            v-for="(task, index) in taskdata"
            :key="task.id"
            class="itbkk-item"
          >
            <th class="text-blue-400">{{ task.id }}</th>
            <td class="itbkk-title">
              <button @click="openModal(task.id)" class="btn btn-ghost">
                {{ task.title }}
                <img src="/icons/pen.png" class="w-4" />
              </button>
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
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <AddEditTask @closeModal="closeModal" :showModal="showModal" :task="task" />
</template>

<style scoped></style>
