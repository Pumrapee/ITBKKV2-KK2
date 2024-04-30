<script setup>
import { onMounted } from "vue"
import { RouterView } from "vue-router"
import { useTaskStore } from "../src/stores/taskStore"
import { getItems } from "./libs/fetchUtils"

const myTask = useTaskStore()
onMounted(async () => {
  if (myTask.getTasks().length === 0) {
    const listTasks = await getItems(import.meta.env.VITE_BASE_URL)
    myTask.addTasks(listTasks)
  }

  console.log(myTask.getTasks())
})
</script>

<template>
  <RouterView />
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400..700;1,400..700&family=Open+Sans:ital,wght@0,300..800;1,300..800&family=Taviraj:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap");

body {
  font-family: "Open Sans", sans-serif;
}
</style>
