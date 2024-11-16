<script setup>
import { defineProps, defineEmits } from "vue"

const props = defineProps({
  showDelete: Boolean,
  detailDelete: Object,
})
const emits = defineEmits(["closeDeleteTask", "cancelDelete"])

const confirmDelete = async (id) => {
  emits("closeDeleteTask", id)
}

const cancelDelete = () => {
  emits("cancelDelete")
}
</script>

<template>
  <!-- Modal Delete -->
  <div v-if="showDelete" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15] px-4">
      <div class="bg-white p-6 sm:p-10 rounded-lg w-full max-w-lg sm:w-1/2 lg:w-1/3">
        <div class="itbkk-message text-lg font-semibold text-center">
          <p class="word-wrap break-words">
            Do you want to delete the task number <br />
            <span class="text-blue-400"
              >"number {{ detailDelete.index }} -
              {{ detailDelete.title }}"</span
            >
            <span> ?</span>
          </p>
        </div>

        <div class="mt-6 flex flex-col sm:flex-row justify-center gap-4">
          <button
            class="itbkk-button-confirm btn bg-red-500 text-white w-full sm:w-auto px-4 py-2 rounded-md hover:bg-red-600"
            @click="confirmDelete(detailDelete.id)"
          >
            Confirm
          </button>
          <button class="itbkk-button-cancel btn bg-gray-300 text-black w-full sm:w-auto px-4 py-2 rounded-md hover:bg-gray-400" @click="cancelDelete()">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@media (max-width: 768px) {
  .bg-white {
    padding: 1.5rem;
  }
  .btn {
    width: 100%;
  }
  .flex.justify-end {
    flex-direction: column;
    gap: 1rem;
  }
  .text-lg {
    font-size: 1rem;
  }
}
</style>
