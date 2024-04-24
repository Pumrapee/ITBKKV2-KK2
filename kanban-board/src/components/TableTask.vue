<script setup>
import { ref, onMounted } from "vue"
import tasks from "../../data/task.json"
import AddEditTask from "../components/AddEditTask.vue"

const showModal = ref(false)
const taskdata = ref([])
const taskIdtoEdit = ref()

const closeModal = () => {
  showModal.value = false
}

const openModal = (taskId) => {
  taskIdtoEdit.value = taskId
  showModal.value = true
}

onMounted(async () => {
  try {
    const response = await fetch(import.meta.env.VITE_BASE_URL)
    if (!response.ok) {
      throw new Error("Failed to fetch data")
    }
    const data = await response.json()
    taskdata.value = data
  } catch (error) {
    console.error(error)
  }

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
    :taskId="taskIdtoEdit"
  />
</template>

<style scoped></style>
