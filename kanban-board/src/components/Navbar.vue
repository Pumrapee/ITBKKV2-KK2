<script setup></script>

<template>
  <!-- Navbar -->
  <div class="navbar bg-white border-b border-gray">
    <div class="navbar-start font-custom">
      <button
        class="btn btn-ghost bg-white pl-5 flex items-center text-2xl text-blue-400 font-semibold"
      >
        <RouterLink :to="{ name: 'task' }">
          <span>IT-BANGMOD KRADAN KANBAN</span>
        </RouterLink>
        <img src="/icons/completed-task.png" class="w-6 ml-2" />
      </button>
    </div>

    <div class="navbar-end"></div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400..700;1,400..700&family=Open+Sans:ital,wght@0,300..800;1,300..800&family=Playfair+Display:ital,wght@0,400..900;1,400..900&family=Taviraj:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap");
.font-custom {
  font-family: "Playfair Display", sans-serif;
}
</style>
