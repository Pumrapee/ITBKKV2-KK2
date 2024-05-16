<script setup>
import { ref, defineProps, defineEmits } from "vue"
import { useTaskStore } from "@/stores/taskStore"
import { deleteItemById } from "../libs/fetchUtils"
import AlertComponent from "./Alert.vue"

const mytasks = useTaskStore()
const modalAlert = ref({ message: "", type: "", modal: false })

const props = defineProps({
  showDelete: Boolean,
  detailDelete: Object,
})

const emits = defineEmits(["closeDeleteTask", "cancleDelete"])

const confirmDelete = async () => {
  const deleteItem = await deleteItemById(
    `${import.meta.env.VITE_API_URL}tasks`,
    props.detailDelete.id
  )

  if (deleteItem === 200) {
    mytasks.removeTasks(props.detailDelete.id)
    emits("closeDeleteTask", deleteItem)
  }

  if (deleteItem === 404) {
    mytasks.removeTasks(props.detailDelete.id)
    emits("closeDeleteTask", deleteItem)
  }
}

const cancelDelete = () => {
  emits("cancleDelete")
}
</script>

<template>
  <!-- Alert -->
  <AlertComponent
    :message="modalAlert.message"
    :type="modalAlert.type"
    :showAlert="modalAlert.modal"
  />
  <!-- Modal Delete -->
  <div v-if="showDelete" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-10 rounded-lg w-1/3">
        <div class="itbkk-message text-lg font-semibold text-center">
          <p style="word-wrap: break-word">
            Do you want to delete the task <br />
            <span class="text-blue-400"
              >"number {{ detailDelete.index }} -
              {{ detailDelete.title }}"</span
            >
            <span> ?</span>
          </p>
        </div>

        <div class="mt-4 flex justify-end">
          <button
            class="itbkk-button-confirm btn mr-4 bg-red-500 text-white"
            @click="confirmDelete()"
          >
            Confirm
          </button>
          <button class="itbkk-button-cancel btn" @click="cancelDelete()">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
