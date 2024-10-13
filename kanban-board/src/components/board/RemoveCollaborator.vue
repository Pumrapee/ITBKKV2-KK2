<script setup>
import { defineProps, defineEmits, ref } from "vue";

const props = defineProps({
  showDelete: Boolean,
  selectedCollabName: String, // Receive the collaborator name
  selectedCollabId: String,   // Receive the collaborator ID
});

const emits = defineEmits(["closeDeleteBoard", "cancelDelete", "confirmRemove"]);

const confirmDelete = () => {
  emits("confirmRemove", props.selectedCollabId); // Emit event with collaborator ID
  emits("closeDeleteBoard");
};

const cancelDelete = () => {
  emits("cancelDelete");
};
</script>

<template>
  <div v-if="showDelete" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-10 rounded-lg w-1/5">
        <div class="text-lg font-semibold text-center">
          <h2 class="text-xl font-semibold mb-4">Remove Collaborator</h2>
          <p style="word-wrap: break-word">
            Do you want to remove <span class="font-bold">{{ selectedCollabName }}</span> from the board?
          </p>
        </div>

        <div class="mt-4 flex justify-end">
          <button
            class="btn mr-4 bg-red-500 text-white"
            @click="confirmDelete"
          >
            Confirm
          </button>
          <button class="btn" @click="cancelDelete">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
