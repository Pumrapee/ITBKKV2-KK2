<script setup>
import { RouterView } from "vue-router"
</script>

<template>
  <video
    autoplay
    loop
    muted
    playsinline
    class="top-0 left-0 w-full h-full object-cover z-[-1] absolute"
    id="myVideo"
  >
    <source src="/video/bg.mp4" type="video/mp4" />
    Your browser does not support the video tag.
  </video>
  <RouterView />
  <!-- footer -->
  <!-- <footer
    class="fixed bottom-0 left-0 right-0 footer items-center p-2 bg-black text-white font-semibold"
  >
    <aside class="items-center grid-flow-col">
      <p class="font-thin">Kradan Kanban By KK2 since 2024</p>
    </aside>
    <nav class="grid-flow-col gap-4 md:place-self-center md:justify-self-end">
      <img src="/icons/logo2.png" class="w-12" />
    </nav>
  </footer> -->
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400..700;1,400..700&family=Open+Sans:ital,wght@0,300..800;1,300..800&family=Taviraj:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap");

body {
  font-family: "Open Sans", sans-serif;
}

#myVideo {
  position: fixed;
  right: 0;
  bottom: 0;
  min-width: 100%;
  min-height: 100%;
}
</style>
