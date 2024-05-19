<script setup>
import { ref } from "vue"
import AddTask from "../components/AddTask.vue"
import router from "@/router"
import { useTaskStore } from "../stores/taskStore"
import AlertComponent from "./Alert.vue"
import { defineEmits } from "vue"

const emits = defineEmits(["showbtn"])
const myTask = useTaskStore()
const showAdd = ref()
const modalAlert = ref({ message: "", type: "", modal: false })

const showModalAdd = () => {
  showAdd.value = true
}

const CancelAdd = () => {
  showAdd.value = false
  router.go(-1)
}

const closeAddModal = (statusCode, status) => {
  if (statusCode === 201) {
    showAdd.value = false
    router.go(-1)
    modalAlert.value = {
      message: "The task has been successfully added",
      type: "success",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "4000")
  }

  if (statusCode === 400) {
    showAdd.value = false
    router.go(-1)
    modalAlert.value = {
      message: "An error has occurred, the task does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "4000")
  }

  if (statusCode === 404) {
    showAdd.value = false
    router.go(-1)
    modalAlert.value = {
      message: "An error has occurred, the task does not exist.",
      type: "error",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "4000")
  }

  if (statusCode === 507) {
    modalAlert.value = {
      message: `The status ${status} will have too many tasks.  Please make progress and update status of existing tasks first.`,
      type: "warning",
      modal: true,
    }
    setTimeout(() => {
      modalAlert.value.modal = false
    }, "4000")
  }
}

const showbtnDelete = () => {
  emits("showbtn")
}
</script>

<template>
  <!-- Alert -->
  <AlertComponent
    :message="modalAlert.message"
    :type="modalAlert.type"
    :showAlert="modalAlert.modal"
  />
  <!-- Navbar -->
  <div class="navbar bg-white border-b border-gray">
    <div class="navbar-start font-custom">
      <button
        class="btn btn-ghost bg-white pl-5 flex items-center text-2xl text-blue-400 font-semibold"
      >
        <RouterLink :to="{ name: 'task' }">
          <span>IT-BANGMOD KRADAN KANBAN</span>
        </RouterLink>
        <img src="/icons/completed-task.png" class="w-6 ml-2" />
      </button>
    </div>

    <div class="navbar-end">
      <button
        v-if="myTask.showNavbar"
        @click="showbtnDelete"
        class="itbkk-button-action btn border-red-500 bg-red-500 text-white mr-2"
      >
        <img src="/icons/delete.png" class="w-5 text-orange-400" />Delete
      </button>
      <router-link :to="{ name: 'add' }">
        <button
          v-if="myTask.showNavbar"
          @click="showModalAdd"
          class="itbkk-button-add btn border-blue-400 bg-blue-400 text-white hover:bg-pink-400"
        >
          <img src="/icons/plus.png" class="w-4" />Add Task
        </button>
      </router-link>
    </div>
  </div>

  <AddTask
    @closeAddModal="closeAddModal"
    @closeCancel="CancelAdd"
    :showAdd="showAdd"
  />
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400..700;1,400..700&family=Open+Sans:ital,wght@0,300..800;1,300..800&family=Playfair+Display:ital,wght@0,400..900;1,400..900&family=Taviraj:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap");
.font-custom {
  font-family: "Playfair Display", sans-serif;
}
</style>
