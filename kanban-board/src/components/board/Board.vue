<script setup>
import router from "@/router"
import { ref } from "vue"
import AddBoard from "./AddBoard.vue"

const openModal = ref()
const openModalAdd = () => {
  openModal.value = true
}

const closeModal = () => {
  openModal.value = false
}
</script>

<template>
  <div class="flex flex-col items-center mt-16 mb-20 ml-52">
    <div class="flex justify-between w-3/5">
      <div class="font-bold text-4xl m-2 ">Board list</div>
      <div>
        <router-link :to="{ name: 'addBoard' }">
          <button
            @click="openModalAdd"
            class="itbkk-button-add btn btn-circle border-black0 bg-black text-white ml-2"
          >
            <img src="/icons/plus.png" class="w-4" />
          </button>
        </router-link>
      </div>
    </div>

    <!-- Table -->
    <div class="border border-black rounded-md w-3/5 mt-4">
      <table class="table">
        <!-- head -->
        <thead class="bg-black">
          <tr class="text-white text-sm">
            <th class="pl-20">No.</th>
            <th class="pl-15">Name</th>
            <th class="pl-20">Action</th>
          </tr>
        </thead>
        <tbody v-if="true"></tbody>

        <tbody v-else>
          <tr>
            <td colspan="5" class="text-center py-4 text-gray-500 font-medium">
              No task
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <AddBoard :showModal="openModal" @closeModal="closeModal" />
</template>

<style scoped></style>
