<script setup>
import { defineProps, defineEmits, ref } from "vue"

const props = defineProps({
  showDelete: Boolean,
  showLeave: Boolean,
  selectedCollabName: String, // Receive the collaborator name
})

const emits = defineEmits(["cancelDelete", "confirmRemove"])

const confirmDelete = () => {
  emits("confirmRemove") // Emit event with collaborator ID
}

const cancelDelete = () => {
  emits("cancelDelete")
}
</script>

<template>
  <div
    v-if="showDelete || showLeave"
    class="fixed z-10 inset-0 overflow-y-auto"
  >
    <div class="flex items-center justify-center min-h-screen bg-black/[.15] px-4">
      <div class="bg-white p-6 rounded-lg w-full max-w-lg sm:w-3/4 md:w-1/2 lg:w-1/3">
        <div class="text-lg font-semibold text-center">
          <h2 class="text-xl font-semibold mb-4">
            {{ !props.showLeave ? "Remove Collaborator" : "Leave Board" }}
          </h2>
          <p class="word-wrap break-words">
            Do you want to {{ !props.showLeave ? "remove" : "leave this" }}
            <span class="font-bold">{{ selectedCollabName }}</span>
            {{ !props.showLeave ? "from the board?" : "board?" }}
          </p>
        </div>

        <div class="mt-6 flex justify-center md:space-x-4 space-y-4 md:space-y-0 flex-col md:flex-row">
          <button
            class="btn bg-red-500 text-white px-4 py-2 rounded-md hover:bg-red-600 w-full md:w-auto"
            @click="confirmDelete"
          >
            Confirm
          </button>
          <button
            class="btn bg-gray-300 text-black px-4 py-2 rounded-md hover:bg-gray-400 w-full md:w-auto"
            @click="cancelDelete"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@media (max-width: 768px) {
  /* Responsive styles สำหรับหน้าจอมือถือ */
  .bg-white {
    padding: 2rem 1rem;
  }

  .btn {
    width: 100%; /* ทำให้ปุ่มมีขนาดเต็มหน้าจอ */
    margin-bottom: 0.5rem; /* เพิ่มช่องว่างระหว่างปุ่ม */
  }

  .flex.justify-center {
    flex-direction: column;
  }
}
</style>
