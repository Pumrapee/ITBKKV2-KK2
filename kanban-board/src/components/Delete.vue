<script setup>
import { ref, defineProps } from "vue"
import { useModalStore } from "../stores/modal"
import { useTaskStore } from "@/stores/taskStore"
import { deleteItemById } from "../libs/fetchUtils"
import AlertComponent from "./Alert.vue"

const modal = useModalStore()
const mytasks = useTaskStore()
const modalAlert = ref({ message: "", type: "", modal: false })

const { showDelete } = defineProps({
  showDelete: Boolean,
})

const confirmDelete = async () => {
  const deleteItem = await deleteItemById(
    `${import.meta.env.VITE_BASE_URL}tasks`,
    modal.deleteId
  )

  if (deleteItem === 200) {
    mytasks.removeTasks(modal.deleteId)
    modal.showDelete = false
    modalAlert.value = {
      message: "The task has been deleted",
      type: "success",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  } else if (deleteItem === 404) {
    mytasks.removeTasks(modal.deleteId)
    modal.showDelete = false
    modalAlert.value = {
      message: "An error has occurred, the task does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "2500")
  }
}

const cancelDelete = () => {
  modal.showDelete = false
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
  <div v-if="modal.showDelete" class="fixed z-10 inset-0 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen bg-black/[.15]">
      <div class="bg-white p-10 rounded-lg w-1/3">
        <div class="itbkk-message text-lg font-semibold text-center">
          <p style="word-wrap: break-word">
            Do you want to delete the task <br />
            <span class="text-blue-400"
              >"number {{ modal.indexNumber }} - {{ modal.deleteTitle }}"</span
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
