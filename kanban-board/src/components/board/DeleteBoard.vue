<script setup>
import { defineProps, defineEmits } from "vue"

const props = defineProps({
  showDelete: Boolean,
})
const emits = defineEmits(["closeDeleteBoard", "cancelDelete"])

const confirmDelete = () => {
  emits("closeDeleteBoard")
}

const cancelDelete = () => {
  emits("cancelDelete")
}
</script>

<template>
  <!-- Modal Delete -->
  <div v-if="showDelete" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-10 rounded-lg w-1/5">
        <div class="itbkk-message text-lg font-semibold text-center">
          <p style="word-wrap: break-word">
            Do you want to delete the Board
          </p>
        </div>

        <div class="mt-4 flex justify-end">
          <button
            class="itbkk-button-confirm btn mr-4 bg-red-500 text-white"
            @click="confirmDelete()"
          >
            Confirm
          </button>
          <button class="itbkk-button-cancel btn" @click="cancelDelete()">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
