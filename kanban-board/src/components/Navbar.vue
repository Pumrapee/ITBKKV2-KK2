<script setup>
import { ref } from "vue"
import AddTask from "../components/AddTask.vue"
import router from "@/router"

const showAdd = ref()

const showModalAdd = () => {
  showAdd.value = true
}

const closeAdd = () => {
  showAdd.value = false
  router.go(-1)
}
</script>

<template>
  <div class="navbar bg-blue-400 border-b border-gray">
    <div class="navbar-start font-custom">
      <a class="pl-5 text-2xl text-white font-semibold"
        >IT-BANGMOD KRADAN KANBAN
      </a>
      <img src="/icons/edit-table.png" class="w-6 m-2" />
    </div>

    <div class="navbar-end">
      <button class="btn border-red-500 bg-red-500 text-white mr-2">
        <img src="/icons/delete.png" class="w-5 text-orange-400" />Delete
      </button>

      <router-link to="/task/add">
        <button
          @click="showModalAdd"
          class="btn border-pink-400 bg-pink-400 text-white"
        >
          <img src="/icons/plus.png" class="w-4" />Add Task
        </button>
      </router-link>
    </div>
  </div>

  <AddTask @closeAddModal="closeAdd" :showAdd="showAdd" />
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400..700;1,400..700&family=Open+Sans:ital,wght@0,300..800;1,300..800&family=Playfair+Display:ital,wght@0,400..900;1,400..900&family=Taviraj:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap");
.font-custom {
  font-family: "Playfair Display", sans-serif;
}
</style>
