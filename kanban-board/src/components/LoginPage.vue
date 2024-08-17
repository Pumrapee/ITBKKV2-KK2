<script setup>
import { ref, computed } from "vue"
import { login } from "../libs/fetchUtils"

const username = ref('')
const password = ref('')

const isButtonDisabled = computed(() => {
  return (
    username.value.trim() === '' ||
    username.value.length > 50 ||
    password.value.trim() === '' ||
    password.value.length > 14
  )
})

const loginHandler = async () => {
  const status = await login(`${import.meta.env.VITE_API_URL}login`, username.value, password.value);

  if (status === 200) {
    
  } else if (status === 401) {
    alert("Username or Password is incorrect.")
  } else {
    alert("An unexpected error occurred. Please try again later.")
  }
}

</script>

 
<template>

<div class="flex items-center justify-center min-h-screen bg-gray-100 relative overflow-hidden">
    <video autoplay loop muted playsinline class="absolute top-0 left-0 w-full h-full object-cover absolute z-0">
      <source src="/video/bg.mp4" type="video/mp4" />
      Your browser does not support the video tag.
    </video>

    <div class="relative z-10 bg-opacity-90 w-full max-w-sm bg-white rounded-lg shadow-lg p-8">
      <!-- Logo -->
      <div class="flex justify-center mb-6">
        <img src="/icons/logo1.png" alt="Logo" class="h-44 w-44" />
      </div>

      <!-- Title -->
      <h2 class="text-xl font-semibold text-center text-gray-800">Welcome!</h2>

      <!-- Form -->
      <form @submit.prevent="loginHandler" class="mt-6">
        <div class="mb-4">
          <label for="username" class="block text-sm font-medium text-gray-700">Username</label>
          <input
            type="text"
            id="username"
            v-model="username"
            required
            maxlength="50"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-gray-800 focus:border-gray-800 sm:text-sm"
          />
        </div>
        <div class="mb-6">
          <label for="password" class="block text-sm font-medium text-gray-700">Password</label>
          <input
            type="password"
            id="password"
            v-model="password"
            required
            maxlength="14"
            class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-gray-800 focus:border-gray-800 sm:text-sm"
          />
        </div>
        <button
          type="submit"
          :disabled="isButtonDisabled"
          class="w-full bg-gray-800 text-white font-semibold py-2 px-4 rounded-md hover:bg-black focus:outline-none focus:ring-2 focus:ring-gray-800 focus:ring-opacity-50 disabled:bg-gray-300"
        >
          Login
        </button>
      </form>
    </div>
  </div>

  <div role="alert" class="alert alert-error">
  <svg
    xmlns="http://www.w3.org/2000/svg"
    class="h-6 w-6 shrink-0 stroke-current"
    fill="none"
    viewBox="0 0 24 24">
    <path
      stroke-linecap="round"
      stroke-linejoin="round"
      stroke-width="2"
      d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
  <span>Username or Password is incorrect</span>
</div>


</template>
 
<style scoped>

</style>