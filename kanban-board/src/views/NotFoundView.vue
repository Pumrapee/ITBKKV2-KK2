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
  <div class="flex justify-center items-center h-screen relative z-10">
    <div class="flex flex-col items-center">
      <img src="/errorPage/404_notfound.png" class="w-96" />
      <div class="card-body text-center">
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
