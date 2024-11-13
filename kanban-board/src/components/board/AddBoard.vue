<script setup>
import { defineProps, defineEmits, ref, computed } from "vue"
import { useAuthStore } from "@/stores/loginStore"

const props = defineProps({
  showModal: Boolean,
})
const myUser = useAuthStore()
const userName = localStorage.getItem("user")

const boardName = ref({ name: `${userName} personal board` })
const emits = defineEmits(["closeModal", "saveAdd"])

const saveAdd = () => {
  emits("saveAdd", boardName.value)
}

const closeModals = () => {
  boardName.value.name = `${userName} personal board`
  emits("closeModal")
}

const isButtonDisabled = computed(() => {
  return (
    boardName.value.name?.trim() === "" || boardName.value.name?.length > 120
  )
})
</script>

<template>
  <!-- Modal -->
  <div v-if="showModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div
      class="itbkk-modal-new flex items-center justify-center min-h-screen bg-black/[.15] p-4 sm:p-8"
    >
      <div class="bg-white p-4 sm:p-10 rounded-lg w-full sm:w-1/3">
        <div class="itbkk-message text-lg font-semibold text-center">
          <div class="flex justify-between items-center mb-4 border-b-2">
            <h2 class="text-xl sm:text-2xl font-bold mb-2">New Board</h2>
          </div>

          <div class="flex flex-col">
            <p class="self-start">Name</p>
            <input
              id="name"
              type="text"
              v-model="boardName.name"
              maxlength="120"
              placeholder="Enter your name board..."
              class="itbkk-board-name w-full border border-slate-950 rounded-lg py-2 px-3 input input-ghost"
            />
          </div>
        </div>

        <div class="mt-4 flex flex-col sm:flex-row justify-end gap-2">
          <button
            class="itbkk-button-ok btn bg-green-500 text-white disabled:bg-gray-300"
            @click="saveAdd"
            :disabled="isButtonDisabled"
            :class="isButtonDisabled ? 'disabled disabled:bg-gray-300' : ''"
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

<style scoped>
  /* Mobile adjustments */
  @media (max-width: 640px) {
    .itbkk-modal-new {
      padding: 1rem;
    }

    .bg-white {
      width: 100%;
    }

    .text-2xl {
      font-size: 1.5rem;
    }
  }
</style>
