<script setup>
import { ref, onMounted } from "vue"
import tasks from "../../data/task.json"
import AddEditTask from "../components/AddEditTask.vue"
import { getItems,getItemById } from "../libs/fetchUtils"


const showModal = ref(false)
const taskdata = ref([])
const task = ref()

const closeModal = () => {
  showModal.value = false
}

const openModal = async (taskId) => {
  console.log(taskId);
  const data = await getItemById(import.meta.env.VITE_BASE_URL, taskId)
  task.value = await data
  showModal.value = true
}

onMounted(async () => {
  const data = await getItems(import.meta.env.VITE_BASE_URL)
  taskdata.value = data

  console.log(taskdata.value)
})
</script>

<template>
  <!-- Task table -->
  <div class="flex justify-center mt-40">
    <div class="overflow-x-auto border-2 rounded-md w-4/5">
      <table class="table">
        <thead class="bg-slate-100">
          <tr>
            <th></th>
            <th>Title</th>
            <th>Assignees</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(task, index) in taskdata" :key="task.id">
            <th>{{ task.id }}</th>
            <td>
              <button @click="openModal(task.id)" class="btn btn-ghost">
                {{ task.title }}
              </button>
            </td>
            <td>
              <p v-for="(assignee, index) in task.assignees" :key="index">
                {{ assignee }}
              </p>
            </td>
            <td>{{ task.status }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <AddEditTask
    @closeModal="closeModal"
    :showModal="showModal"
    :task="task"
  />
</template>

<style scoped></style>
