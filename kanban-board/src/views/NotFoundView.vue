<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const count = ref(0)
const router = useRouter()

onMounted(() => {
  setTimeout(() => {
    router.push({ name: 'task' })
  }, 3000)
})
</script>

<template>
  <div class="flex-container">
    <div class="card w-96 bg-blue-400 text-white">
      <div class="card-body items-center text-center">
        <h2 class="card-title">Not Found!</h2>
        <p>The page you are looking for does not exist.</p>

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
  background-color: #f0f0f0; /* เพิ่มสีพื้นหลังเพื่อให้เห็นได้ชัด */
}

.card {
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

h1 {
  color: red;
  font-size: 2em;
}

p {
  font-size: 1.2em;
}
</style>