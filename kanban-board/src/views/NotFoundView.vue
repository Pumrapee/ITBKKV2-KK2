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
  <div class="flex-container relative z-10">
    <div
      class="card w-2/5 bg-slate-100 text-black bg-opacity-90 backdrop-blur rounded-lg shadow-lg p-8"
    >
      <div class="card-body items-center text-center">
        <h2 class="card-title text-red-500 text-4xl">Not Found! 404</h2>
        <p>The requested task does not exist</p>
      </div>
    </div>
  </div>
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
