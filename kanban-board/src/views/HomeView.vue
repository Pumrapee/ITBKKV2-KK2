<script setup>
import TableTask from "../components/TableTask.vue"
import Navbar from "../components/Navbar.vue"
import { ref } from "vue"

const showDeleteBtn = ref(false)

const updateDeleteBtn = () => {
  showDeleteBtn.value = !showDeleteBtn.value
}
</script>

<template>
  <main>
    <Navbar @showbtn="updateDeleteBtn" />
    <TableTask :showbtnDelete="showDeleteBtn" />
  </main>
</template>
