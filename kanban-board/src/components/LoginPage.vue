<script setup>
import { ref, computed, onMounted } from "vue"
import { login } from "../libs/fetchUtils"
import { useAuthStore } from "@/stores/loginStore"
import { useBoardStore } from "@/stores/boardStore"
import { getToken } from "@/libs/fetchUtils"
import { useRouter } from "vue-router"

const username = ref("")
const password = ref("")
const authStore = useAuthStore()
const router = useRouter()

const isButtonDisabled = computed(() => {
  return (
    username.value.trim() === "" ||
    username.value.length > 50 ||
    password.value.trim() === "" ||
    password.value.length > 14
  )
})

const alertMessage = ref("")
const showAlert = ref(false)

const loginHandler = async () => {
  const { res, token } = await login(
    `${import.meta.env.VITE_API_URL}login`,
    username.value,
    password.value
  )

  if (res.status === 200) {
    authStore.login(token)
    getToken()
    router.push({ name: "board" })
  } else if (res.status === 400 || res.status === 401) {
    alertMessage.value = "Username or Password is incorrect."
    showAlert.value = true
  }
}
</script>

<template>
  <div
    class="flex items-center justify-center min-h-screen relative overflow-hidden"
  >
    <!-- login box -->
    <div
      class="slide-in-elliptic-left-fwd slide-in-left relative z-10 bg-opacity-90 w-full max-w-sm backdrop-blur rounded-lg shadow-lg p-8"
    >
      <!-- Logo -->
      <div class="flex justify-center mb-6">
        <img src="/icons/logo1.png" alt="Logo" class="h-44 w-44" />
      </div>

      <!-- Title -->
      <h2 class="text-xl font-semibold text-center text-gray-800">Welcome!</h2>

      <!-- Alert inside login box -->
      <div
        v-if="showAlert"
        role="alert"
        class="relative m-4 p-4 rounded-lg bg-red-50 text-red-700 flex items-start shadow-lg shadow-red-100"
      >
        <!-- Alert Icon -->
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-6 w-6 mr-3 flex-shrink-0"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>

        <!-- Alert Message -->
        <span class="itbkk-message flex-grow">{{ alertMessage }}</span>

        <!-- Close Button -->
        <button
          @click="showAlert = false"
          aria-label="Close"
          class="ml-3 text-red-500 hover:text-red-700"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <!-- Form -->
      <form @submit.prevent="loginHandler" class="mt-6">
        <div class="mb-4">
          <label for="username" class="block text-sm font-medium text-gray-700"
            >Username</label
          >
          <input
            type="text"
            id="username"
            v-model="username"
            required
            maxlength="50"
            class="itbkk-username mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-gray-800 focus:border-gray-800 sm:text-sm"
          />
        </div>
        <div class="mb-6">
          <label for="password" class="block text-sm font-medium text-gray-700"
            >Password</label
          >
          <input
            type="password"
            id="password"
            v-model="password"
            required
            maxlength="14"
            class="itbkk-password mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-gray-800 focus:border-gray-800 sm:text-sm"
          />
        </div>
        <button
          type="submit"
          :disabled="isButtonDisabled"
          :class="{ disabled: isButtonDisabled }"
          class="itbkk-button-signin w-full bg-gray-800 text-white font-semibold py-2 px-4 rounded-md hover:bg-black focus:outline-none focus:ring-2 focus:ring-gray-800 focus:ring-opacity-50 disabled:bg-gray-300"
        >
          Login
        </button>
      </form>
    </div>
  </div>
  <!-- footer -->
  <footer
    class="fixed bottom-0 left-0 right-0 footer items-center p-2 bg-black text-white font-semibold"
  >
    <aside class="items-center grid-flow-col">
      <p class="font-thin">Kradan Kanban By KK2 since 2024</p>
    </aside>
    <nav class="grid-flow-col gap-4 md:place-self-center md:justify-self-end">
      <img src="/icons/logo2.png" class="w-12" />
    </nav>
  </footer>
</template>

<style scoped>
.slide-in-elliptic-left-fwd {
  -webkit-animation: slide-in-elliptic-left-fwd 0.7s
    cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
  animation: slide-in-elliptic-left-fwd 0.7s
    cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}
@-webkit-keyframes slide-in-elliptic-left-fwd {
  0% {
    -webkit-transform: translateX(-800px) rotateY(30deg) scale(0);
    transform: translateX(-800px) rotateY(30deg) scale(0);
    -webkit-transform-origin: -100% 50%;
    transform-origin: -100% 50%;
    opacity: 0;
  }
  100% {
    -webkit-transform: translateX(0) rotateY(0) scale(1);
    transform: translateX(0) rotateY(0) scale(1);
    -webkit-transform-origin: 1800px 50%;
    transform-origin: 1800px 50%;
    opacity: 1;
  }
}
@keyframes slide-in-elliptic-left-fwd {
  0% {
    -webkit-transform: translateX(-800px) rotateY(30deg) scale(0);
    transform: translateX(-800px) rotateY(30deg) scale(0);
    -webkit-transform-origin: -100% 50%;
    transform-origin: -100% 50%;
    opacity: 0;
  }
  100% {
    -webkit-transform: translateX(0) rotateY(0) scale(1);
    transform: translateX(0) rotateY(0) scale(1);
    -webkit-transform-origin: 1800px 50%;
    transform-origin: 1800px 50%;
    opacity: 1;
  }
}
</style>
