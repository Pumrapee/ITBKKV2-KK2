<script setup>
import { defineProps, defineEmits, ref } from "vue"

const props = defineProps({
  showModal: Boolean,
})
const boardName = ref({ name: "" })
const emits = defineEmits(["closeModal", "saveAdd"])

const saveAdd = () => {
  console.log(boardName.value)
  emits("saveAdd", boardName.value)
}

const closeModals = () => {
  emits("closeModal")
}
</script>

<template>
  <!-- Modal -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-10 rounded-lg w-1/3">
        <div class="itbkk-message text-lg font-semibold text-center">
          <div class="flex justify-between items-center mb-4 border-b-2">
            <h2 class="text-2xl font-bold mb-2">New Board</h2>
          </div>

          <div class="flex flex-col">
            <p class="self-start">Name</p>
            <input
              id="name"
              type="text"
              v-model="boardName.name"
              placeholder="Enter your name board..."
              class="itbkk-title w-full border border-slate-950 rounded-lg py-2 px-3 input input-ghost"
            />
          </div>
        </div>

        <div class="mt-4 flex justify-end">
          <button
            class="itbkk-button-confirm btn mr-2 bg-green-500 text-white"
            @click="saveAdd"
          >
            Save
          </button>
          <button class="itbkk-button-cancel btn" @click="closeModals">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
