<script setup>
import { useRouter } from "vue-router"
import { useAuthStore } from "@/stores/loginStore"
import { onMounted } from "vue"

const router = useRouter()
const myUser = useAuthStore()
onMounted(async () => {
  setTimeout(() => {
    router.push({ name: "login" })
    myUser.logout()
  }, 3000)
})

const goToLogin = () => {
  router.push({ name: "login" })
  myUser.logout()
}
</script>

<template>
  <div
     class="fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50 p-4 md:p-0"
  >
    <div class="card bg-base-100 w-full max-w-sm md:w-96 shadow-xl">
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
