<script setup>
import { ref } from "vue"

const { showModal } = defineProps({
  showModal: Boolean,
})

const emits = defineEmits(["closeDeleteModal"])

</script>

<template>
    <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div
        class="grid grid-rows-6 grid-cols-4 gap-2 bg-white p-10 rounded-lg w-2/3"
      >
       

        <div class="col-span-full text-center">
          <p class="text-lg font-semibold mb-4">Are you sure you want to delete?</p>
        </div>

        <div class="col-span-4 place-self-end rounded-lg">
          <button class="btn" @click="openDeleteModal()">
            Delete
          </button>
          <button class="btn" @click="$emit('closeDeleteModal', false)">
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
  
<style scoped>

</style>