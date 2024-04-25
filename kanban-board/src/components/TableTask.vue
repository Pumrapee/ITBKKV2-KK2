<script setup>
import { ref } from "vue"
import tasks from "../../data/task.json"
import AddEditTask from "../components/AddEditTask.vue"

const showModal = ref(false)

const closeModal = () => {
  showModal.value = false
}
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
          <tr v-for="(task, index) in tasks" :key="task.id">
            <th>{{ task.id }}</th>
            <td>
              <button @click="showModal = true" class="btn btn-ghost">
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

  <AddEditTask @closeModal="closeModal" :showModal="showModal" />
  
</template>

<style scoped></style>
