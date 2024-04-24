<script setup>
import { ref, defineProps } from "vue"
const { showModal, taskId } = defineProps({
  showModal: Boolean,
  taskId: String,
})

console.log(taskId)
defineEmits(["closeModal"])
</script>

<template>
  <!-- Modal window -->
  <div v-show="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div
        class="w-4/5 h-4/5 grid grid-rows-6 grid-cols-4 gap-2 bg-slate-50 p-10 rounded-lg"
      >
        <div class="pl-2 col-span-4 font-bold text-3xl rounded-lg">Title</div>
        <div class="border-2 row-span-4 col-span-3 rounded-lg">
          <p class="p-5 font-bold">Description</p>
          <p class="pl-5">content</p>
        </div>

        <div class="border-2 col-start-4 row-start-2 row-end-4 rounded-lg">
          <p class="p-3 font-bold">Assignees</p>
          <p class="pl-5">content {{ taskId }}</p>
        </div>
        <div class="border-2 col-start-4 row-start-4 row-end-5 rounded-lg">
          <p class="p-2 font-bold">Status</p>
          <p class="pl-5">content</p>
        </div>
        <div class="col-start-4 rounded-lg">
          <p class="pl-5 font-bold">Time Zone :</p>
          <p class="pl-5 font-bold">Created On :</p>
          <p class="pl-5 font-bold">Updated On :</p>
        </div>

        <div class="col-span-4 place-self-end rounded-lg">
          <button class="btn m-3">OK</button>
          <button class="btn" @click="$emit('closeModal', false)">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
