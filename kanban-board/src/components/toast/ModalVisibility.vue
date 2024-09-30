<!-- VisibilityModal.vue -->
<script setup>
import { defineEmits, defineProps } from 'vue'

const props = defineProps({
  isPublic: Boolean,
  showModalVisibility: Boolean,
})

const emit = defineEmits(['confirm', 'cancel'])

const confirmVisibilityChange = () => {
  emit('confirm')
}

const cancelVisibilityChange = () => {
  emit('cancel')
}
</script>

<template>
  <div
    v-if="showModalVisibility"
    class="itbkk-modal-alert fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50"
  >
    <div class="card bg-base-100 w-96 shadow-xl">
      <div class="itbkk-message card-body font-medium">
        <p>
          Do you want to change board visibility to
          {{ isPublic ? 'public' : 'private' }}?
        </p>
        <div class="card-actions justify-end">
          <button @click="confirmVisibilityChange" class="itbkk-button-confirm btn bg-black text-white">
            Confirm
          </button>
          <button @click="cancelVisibilityChange" class="itbkk-button-cancel btn">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
