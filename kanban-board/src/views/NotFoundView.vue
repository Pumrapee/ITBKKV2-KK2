<script setup>
import { onMounted } from "vue"
import { useRouter } from "vue-router"
import { useAuthStore } from "@/stores/loginStore"

const myUser = useAuthStore()

const router = useRouter()

onMounted(() => {
  setTimeout(() => {
    localStorage.clear()
    router.push({ name: "board" })
  }, 3000)
})
</script>

<template>
  <section class="bg-white dark:bg-gray-900 flex justify-center items-center h-screen relative z-10">
    <div class="container min-h-screen px-6 py-12 mx-auto lg:flex lg:items-center lg:gap-12">
      <div class="w-full lg:w-1/2 text-center lg:text-left">
        <p class="text-sm font-medium text-blue-500 dark:text-blue-400">404 error</p>
        <h1 class="mt-3 text-2xl font-semibold text-gray-800 dark:text-white md:text-3xl">Page not found</h1>
        <p class="mt-4 text-gray-500 dark:text-gray-400">Sorry, the page you are looking for doesn't exist. Here are some helpful links:</p>

        <div class="flex items-center mt-6 gap-x-3 justify-center lg:justify-start">
          <button class="flex items-center justify-center w-1/2 px-5 py-2 text-sm text-gray-700 transition-colors duration-200 bg-white border rounded-lg gap-x-2 sm:w-auto dark:hover:bg-gray-800 dark:bg-gray-900 hover:bg-gray-100 dark:text-gray-200 dark:border-gray-700">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-5 h-5 rtl:rotate-180">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6.75 15.75L3 12m0 0l3.75-3.75M3 12h18" />
            </svg>
            <span>Go back</span>
          </button>

          <button class="w-1/2 px-5 py-2 text-sm tracking-wide text-white transition-colors duration-200 bg-blue-500 rounded-lg shrink-0 sm:w-auto hover:bg-blue-600 dark:hover:bg-blue-500 dark:bg-blue-600">
            Take me home
          </button>
        </div>
      </div>

      <div class="relative w-full mt-8 lg:w-1/2 lg:mt-0">
        <img class="w-full lg:h-[32rem] h-80 md:h-96 rounded-lg object-cover" src="/icons/logo1.png" >
      </div>
    </div>
  </section>
</template>

<style scoped>
.flex-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.card {
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

p {
  font-size: 1.2em;
}

#myVideo {
  position: fixed;
  right: 0;
  bottom: 0;
  min-width: 100%;
  min-height: 100%;
}
</style>
