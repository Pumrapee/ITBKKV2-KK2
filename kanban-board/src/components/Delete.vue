<script setup>
import { ref, defineProps } from "vue"
import { useModalStore } from "../stores/modal"
import { useTaskStore } from "@/stores/taskStore"
import { deleteItemById } from "../libs/fetchUtils"

const modal = useModalStore()
const mytasks = useTaskStore()

const deletePass = ref(false)
const deleteFail = ref(false)

const { showDelete } = defineProps({
  showDelete: Boolean,
})

const confirmDelete = async () => {
  const deleteItem = await deleteItemById(
    import.meta.env.VITE_BASE_URL,
    modal.deleteId
  )

  if (deleteItem === 200) {
    mytasks.removeTasks(modal.deleteId)
    deletePass.value = true
    modal.showDelete = false
  } else if (deleteItem === 404) {
    mytasks.removeTasks(modal.deleteId)
    deleteFail.value = true
    modal.showDelete = false
  }
}

const cancelDelete = () => {
  modal.showDelete = false
}

const closeFailDelete = () => {
  deleteFail.value = false
}
</script>

<template>
  <!-- Alert pass -->
  <div v-show="deletePass" class="flex justify-center mt-3 mb-3">
    <div role="alert" class="alert shadow-lg w-1/3">
      <svg
        xmlns="http://www.w3.org/2000/svg"
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

      <span
        >The task
        <span class="font-semibold text-red-500"
          >"{{ modal.deleteTitle }}"</span
        >
        has been <span class="font-semibold text-red-500">deleted</span></span
      >

      <button @click="deletePass = false">X</button>
    </div>
  </div>

  <!-- Alert fail -->
  <div v-if="deleteFail" class="flex justify-center mt-3">
    <div role="alert" class="alert alert-error w-2/3">
      <svg
        xmlns="http://www.w3.org/2000/svg"
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
      <span>An error has occurred, the task does not exist.</span>
      <button @click="closeFailDelete()">X</button>
    </div>
  </div>

  <!-- Modal Delete -->
  <div v-if="modal.showDelete" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-10 rounded-lg w-1/3">
        <div class="itbkk-message text-lg font-semibold text-center">
          <p style="word-wrap: break-word">
            Do you want to delete the task <br />
            <span class="text-blue-400"
              >"number {{ modal.deleteId }} - {{ modal.deleteTitle }}"</span
            >
            <span> ?</span>
          </p>
        </div>

        <div class="itbkk-button-confirm mt-4 flex justify-end">
          <button
            class="btn mr-4 bg-red-500 text-white"
            @click="confirmDelete()"
          >
            Delete
          </button>
          <button class="itbkk-button-cancel btn" @click="cancelDelete()">
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
