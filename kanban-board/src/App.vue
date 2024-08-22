<script setup>
import { onMounted } from "vue"
import { RouterView } from "vue-router"
import { useTaskStore } from "../src/stores/taskStore"
import { getItems, getStatusLimits } from "./libs/fetchUtils"
import { useStatusStore } from "../src/stores/statusStore"
import { useLimitStore } from "./stores/limitStore"

const myTask = useTaskStore()
const myStatus = useStatusStore()
const myLimit = useLimitStore()
onMounted(async () => {
  if (myTask.getTasks().length === 0) {
    const listTasks = await getItems(`${import.meta.env.VITE_API_URL}tasks`)
    myTask.addTasks(listTasks)
  }

  if (myStatus.getStatus().length === 0) {
    const listStatus = await getItems(`${import.meta.env.VITE_API_URL}statuses`)
    myStatus.addStatus(listStatus)
  }

  const limitStatus = await getStatusLimits(
    `${import.meta.env.VITE_API_URL}statuses`
  )
  myLimit.addLimit(limitStatus)
})
</script>

<template>
  <RouterView />
  <!-- footer -->
  <footer
    className="fixed bottom-0 left-0 right-0 footer items-center p-4 bg-black text-white font-semibold"
  >
    <aside className="items-center grid-flow-col">
      
      <p class="font-thin">Kradan Kanban By KK2 since 2024</p>
    </aside>
    <nav
      className="grid-flow-col gap-4 md:place-self-center md:justify-self-end"
    >
      <img src="/icons/logo2.png" class="w-12" />
    </nav>
  </footer>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400..700;1,400..700&family=Open+Sans:ital,wght@0,300..800;1,300..800&family=Taviraj:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap");

body {
  font-family: "Open Sans", sans-serif;
}
</style>
