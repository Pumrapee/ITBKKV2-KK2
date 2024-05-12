<script setup>
import { defineProps } from "vue"
const props = defineProps({
  message: String,
  type: String,
  showAlert: Boolean,
})
</script>

<template>
  <div
    v-if="showAlert"
    class="fixed bottom-6 right-4 mb-0 mr-1"
    style="z-index: 100"
  >
    <div role="alert" :class="'alert alert-' + type + ' w-auto'">
      <svg
        v-if="type === 'success'"
        xmlns="http://www.w3.org/2000/svg"
        class="stroke-current shrink-0 h-6 w-6 text-white"
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
      <svg
        v-if="type === 'error'"
        xmlns="http://www.w3.org/2000/svg"
        class="stroke-current shrink-0 h-6 w-6 text-white"
        fill="none"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <svg
        v-if="type === 'warning'"
        xmlns="http://www.w3.org/2000/svg"
        class="stroke-current shrink-0 h-6 w-6 text-white"
        fill="none"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
        />
      </svg>
      <span class="itbkk-message text-white"> {{ message }}</span>
    </div>
  </div>
</template>

<style scoped>
.alert-warning {
  background-color: rgb(255, 204, 0);
}
.alert-success {
  background-color: rgb(65, 202, 50);
}
.alert-error {
  background-color: rgb(219, 63, 63);
}
</style>
