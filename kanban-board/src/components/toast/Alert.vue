<script setup>
import { listAlert } from "../../libs/alertUtils" // นำเข้า listAlert

// ฟังก์ชันเพื่อลบ alert
const removeAlert = (index) => {
  listAlert.value.splice(index, 1)
}
</script>

<template>
  <!-- New Alert -->
  <div
    v-for="(alert, index) in listAlert"
    :key="index"
    class="fixed bottom-6 right-4 mb-0 mr-1 z-50 w-full max-w-xs space-y-4 md:bottom-6 md:right-6 md:mr-1 md:mb-0 md:w-auto"
  >
    <div
      class="flex items-center w-full max-w-xs p-4 text-gray-500 bg-white rounded-lg shadow dark:text-gray-400 dark:bg-gray-800"
      role="alert"
      :class="'alert-' + alert.type"
    >
      <svg
        v-if="alert.type === 'success'"
        viewBox="0 0 24 24"
        class="text-green-600 w-5 h-5 sm:w-5 sm:h-5 mr-3"
      >
        <path
          fill="currentColor"
          d="M12,0A12,12,0,1,0,24,12,12.014,12.014,0,0,0,12,0Zm6.927,8.2-6.845,9.289a1.011,1.011,0,0,1-1.43.188L5.764,13.769a1,1,0,1,1,1.25-1.562l4.076,3.261,6.227-8.451A1,1,0,1,1,18.927,8.2Z"
        ></path>
      </svg>
      <svg
        v-if="alert.type === 'error'"
        viewBox="0 0 24 24"
        class="text-red-600 w-5 h-5 sm:w-5 sm:h-5 mr-3"
      >
        <path
          fill="currentColor"
          d="M11.983,0a12.206,12.206,0,0,0-8.51,3.653A11.8,11.8,0,0,0,0,12.207,11.779,11.779,0,0,0,11.8,24h.214A12.111,12.111,0,0,0,24,11.791h0A11.766,11.766,0,0,0,11.983,0ZM10.5,16.542a1.476,1.476,0,0,1,1.449-1.53h.027a1.527,1.527,0,0,1,1.523,1.47,1.475,1.475,0,0,1-1.449,1.53h-.027A1.529,1.529,0,0,1,10.5,16.542ZM11,12.5v-6a1,1,0,0,1,2,0v6a1,1,0,1,1-2,0Z"
        ></path>
      </svg>
      <svg
        v-if="alert.type === 'warning'"
        viewBox="0 0 24 24"
        class="text-yellow-600 w-5 h-5 sm:w-5 sm:h-5 mr-3"
      >
        <path
          fill="currentColor"
          d="M23.119,20,13.772,2.15h0a2,2,0,0,0-3.543,0L.881,20a2,2,0,0,0,1.772,2.928H21.347A2,2,0,0,0,23.119,20ZM11,8.423a1,1,0,0,1,2,0v6a1,1,0,1,1-2,0Zm1.05,11.51h-.028a1.528,1.528,0,0,1-1.522-1.47,1.476,1.476,0,0,1,1.448-1.53h.028A1.527,1.527,0,0,1,13.5,18.4,1.475,1.475,0,0,1,12.05,19.933Z"
        ></path>
      </svg>
      <div class="ms-3 text-sm font-normal">{{ alert.message }}</div>
      <button
        type="button"
        class="ms-auto -mx-1.5 -my-1.5 mr-2"
        data-dismiss-target="#toast-default"
        aria-label="Close"
        @click="removeAlert(index)"
      >
        <svg
          class="w-3 h-3"
          aria-hidden="true"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 14 14"
        >
          <path
            stroke="currentColor"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6"
          />
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.alert-warning {
  --tw-bg-opacity: 1;
  background-color: rgb(254 215 170 / var(--tw-bg-opacity)) /* #fed7aa */;
}
.alert-success {
  --tw-bg-opacity: 1;
  background-color: rgb(187 247 208 / var(--tw-bg-opacity)) /* #bbf7d0 */;
}
.alert-error {
  --tw-bg-opacity: 1;
  background-color: rgb(254 202 202 / var(--tw-bg-opacity)) /* #fecaca */;
}
</style>
