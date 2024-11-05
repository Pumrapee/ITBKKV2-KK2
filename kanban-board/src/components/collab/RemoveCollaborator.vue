<script setup>
import { defineProps, defineEmits, ref } from "vue"

const props = defineProps({
  showDelete: Boolean,
  showLeave: Boolean,
  showCancel: Boolean,
  selectedCollabName: String, // Receive the collaborator name
})

const emits = defineEmits(["cancelDelete", "confirmRemove"])

const confirmDelete = () => {
  emits("confirmRemove") // Emit event with collaborator ID
}

const cancelDelete = () => {
  emits("cancelDelete")
}
</script>

<template>
  <div
    v-if="showDelete || showLeave || showCancel"
    class="fixed z-10 inset-0 overflow-y-auto"
  >
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-8 rounded-lg w-2/6">
        <div class="text-lg font-semibold text-center">
          <h2 class="text-xl font-bold mb-4 text-slate-500">
            {{
              props.showLeave
                ? "Leave Board"
                : props.showDelete
                ? "Remove Collaborator"
                : ""
            }}
          </h2>
          <p style="word-wrap: break-word">
            Do you want to
            {{
              props.showLeave
                ? "leave this"
                : props.showDelete
                ? "remove"
                : "cancel invitation to"
            }}
            <span class="font-bold">{{ selectedCollabName }}</span>
            {{
              props.showLeave
                ? "board?"
                : props.showDelete
                ? "from the board?"
                : "?"
            }}
          </p>
        </div>

        <div class="mt-4 flex justify-center space-x-4">
          <button class="btn bg-red-500 text-white" @click="confirmDelete">
            Confirm
          </button>
          <button class="btn" @click="cancelDelete">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
