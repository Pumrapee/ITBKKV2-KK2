<script setup>
import { ref, defineProps, defineEmits } from "vue"

const props = defineProps({
  showLimitModal: Boolean,
})

const isLimitEnabled = ref(false)
const maxTasks = ref(0)

const emit = defineEmits(["closeLimitModal"])
</script>

<template>
  <div v-if="showLimitModal" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="modal-box">
        <h3 class="font-bold text-lg">Status Settings</h3>
        <p class="py-4">
          Users can limit the number of tasks in a status by setting the Maximum
          tasks in each status (except "No Status" and "Done" statuses).
        </p>
        <div class="form-control">
          <label class="cursor-pointer label">
            <span class="label-text">Enable Limit</span>
            <input type="checkbox" v-model="isLimitEnabled" class="toggle" />
          </label>
        </div>
        <div v-if="isLimitEnabled" class="form-control mt-4">
          <label class="label">
            <span class="label-text">Maximum Tasks</span>
          </label>
          <input
            type="number"
            v-model="maxTasks"
            class="input input-bordered"
            placeholder="Enter max tasks"
          />
        </div>
        <div class="modal-action">
          <button @click="$emit('closeLimitModal')" class="btn">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal {
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.modal-box {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  width: 400px;
  max-width: 100%;
}
</style>
