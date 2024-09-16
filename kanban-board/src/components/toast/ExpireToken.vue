<script setup>
import { useRouter } from "vue-router"
import { useAuthStore } from "@/stores/loginStore"
import { defineProps, onMounted } from "vue"

const props = defineProps({
  showExpiredModal: Boolean,
})
const router = useRouter()
const myUser = useAuthStore()

// onMounted(async () => {
//   setTimeout(() => {
//     router.push("/login"), 2000
//   })
// })

const goToLogin = () => {
  router.push("/login")
  myUser.logout()
}
</script>

<template>
  <div
    v-if="props.showExpiredModal"
    class="fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50"
  >
    <div class="card bg-base-100 w-96 shadow-xl">
      <div class="card-body font-medium">
        <p>Your session has expired. Please log in again to continue.</p>
        <div class="card-actions justify-end">
          <button @click="goToLogin" class="btn btn-error text-white">
            Go to Login
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
