<script setup>
import { useAuthStore } from "@/stores/loginStore"
import { useBoardStore } from "@/stores/boardStore"
import router from "@/router"

const authStore = useAuthStore()
const myBoard = useBoardStore()
const userName = localStorage.getItem("user")
const logout = () => {
  authStore.logout()
  myBoard.clearBoard()
  router.push({ name: "login" })
}
</script>

<template>
  <!-- Navbar -->
  <div class="navbar bg-white border-b border-gray">
    <div class="navbar-start font-custom">
      <button
        class="btn btn-ghost bg-white pl-5 flex items-center text-2xl text-black"
      >
        <!-- Logo -->
        <img src="/icons/logonavbar2.png" class="w-10 m-2" />
        <!-- <RouterLink :to="{ name: 'task' }"> -->
        <span>KRADAN KANBAN</span>
        <!-- </RouterLink> -->
      </button>
    </div>

    <!-- user name -->
    <div class="itbkk-fullname navbar-end pr-5">Hi, {{ userName }}</div>
    <div class="cursor-pointer dropdown dropdown-end">
      <div tabindex="0">
        <img src="/icons/profile-user.png" alt="profile" class="h-8" />
      </div>
      <ul
        tabindex="0"
        class="menu dropdown-content bg-base-100 rounded-box z-[1] mt-4 w-40 p-2 shadow"
      >
        <li><a>Profile</a></li>
        <li @click="logout"><a>Logout</a></li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400..700;1,400..700&family=Open+Sans:ital,wght@0,300..800;1,300..800&family=Taviraj:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap");

body {
  font-family: "Open Sans", sans-serif;
}
</style>
